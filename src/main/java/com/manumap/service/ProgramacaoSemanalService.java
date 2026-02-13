package com.manumap.service;

import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.manumap.model.ProgramacaoSemanalItem;
import com.manumap.model.TipoManutencao;

@Service
public class ProgramacaoSemanalService {

    private static final int MAX_HEADER_SCAN_ROWS = 20;
    private static final int MAX_ROWS = 4000;
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{2,4})");

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("d/M/uu"),
            DateTimeFormatter.ofPattern("d-M-uuuu"),
            DateTimeFormatter.ofPattern("uuuu-M-d"),
            DateTimeFormatter.ofPattern("M/d/uuuu"),
            DateTimeFormatter.ofPattern("d.M.uuuu"));

    private final AtomicLong idSequence = new AtomicLong(1);
    private final List<ProgramacaoSemanalItem> itens = new CopyOnWriteArrayList<>();
    private final DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("pt-BR"));

    public List<ProgramacaoSemanalItem> listar() {
        return List.copyOf(itens);
    }

    public Optional<ProgramacaoSemanalItem> buscar(long id) {
        return itens.stream().filter(i -> i.getId() == id).findFirst();
    }

    public List<ProgramacaoSemanalItem> processarUpload(MultipartFile arquivo, List<String> areasConhecidas) throws IOException {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio.");
        }

        List<String> areasOrdenadas = areasConhecidas.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();

        try (InputStream input = arquivo.getInputStream(); Workbook workbook = WorkbookFactory.create(input)) {
            Sheet sheet = escolherPlanilha(workbook);
            HeaderInfo headerInfo = localizarCabecalho(sheet);

            List<ProgramacaoSemanalItem> novosItens = new ArrayList<>();
            int limite = Math.min(sheet.getLastRowNum(), headerInfo.rowIndex + MAX_ROWS);

            for (int i = headerInfo.rowIndex + 1; i <= limite; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                String ordem = valor(headerInfo, row, "order");
                String descricao = valor(headerInfo, row, "orderdescription", "description");
                String operacao = valor(headerInfo, row, "operationshorttext", "opac");
                String atividadeTipo = valor(headerInfo, row, "activitytype", "acttyp");
                String centroPrincipal = valor(headerInfo, row, "mainworkcenter", "opworkctr");
                String centroExecucao = valor(headerInfo, row, "workcenter", "mnwkctr");
                String local = valor(headerInfo, row, "location", "functionallocation", "sortfield", "sortfld");
                String dataRaw = valor(headerInfo, row, "startconstraint", "execstart", "actualexecutionstartdate");

                if (todosVazios(ordem, descricao, operacao, atividadeTipo, centroPrincipal, centroExecucao, local, dataRaw)) {
                    continue;
                }
                if ("order".equals(normalizarCompacto(ordem)) || "operationshorttext".equals(normalizarCompacto(operacao))) {
                    continue;
                }

                String atividade = primeiroNaoVazio(operacao, descricao);
                TipoManutencao tipo = detectarTipo(atividadeTipo, centroPrincipal, centroExecucao, atividade);
                String area = detectarArea(areasOrdenadas, local, centroPrincipal, centroExecucao, descricao, atividade);
                LocalDate dataProgramada = detectarData(row, headerInfo, dataRaw);

                novosItens.add(new ProgramacaoSemanalItem(
                        idSequence.getAndIncrement(),
                        nuloSeVazio(ordem),
                        nuloSeVazio(descricao),
                        nuloSeVazio(atividade),
                        nuloSeVazio(local),
                        nuloSeVazio(centroPrincipal),
                        nuloSeVazio(centroExecucao),
                        nuloSeVazio(atividadeTipo),
                        nuloSeVazio(area),
                        dataProgramada == null ? null : dataProgramada.toString(),
                        i + 1,
                        tipo,
                        sheet.getSheetName()));
            }

            itens.clear();
            itens.addAll(novosItens);
            return listar();
        }
    }

    private Sheet escolherPlanilha(Workbook workbook) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            String nome = normalizarCompacto(sheet.getSheetName());
            if (nome.contains("ordensprg")) {
                return sheet;
            }
        }
        return workbook.getSheetAt(0);
    }

    private HeaderInfo localizarCabecalho(Sheet sheet) {
        int max = Math.min(sheet.getLastRowNum(), MAX_HEADER_SCAN_ROWS);
        for (int i = 0; i <= max; i++) {
            Row row = sheet.getRow(i);
            if (row == null) {
                continue;
            }

            Map<String, Integer> indices = new HashMap<>();
            for (Cell cell : row) {
                String raw = formatter.formatCellValue(cell);
                if (raw == null || raw.isBlank()) {
                    continue;
                }
                indices.put(normalizarCompacto(raw), cell.getColumnIndex());
            }

            if (indices.containsKey("order") && (indices.containsKey("operationshorttext") || indices.containsKey("description"))) {
                return new HeaderInfo(i, indices);
            }
        }

        throw new IllegalArgumentException(
                "Nao encontrei cabecalho valido na planilha (colunas esperadas: Order e Operation short text/Description).");
    }

    private String valor(HeaderInfo headerInfo, Row row, String... nomesNormalizados) {
        Integer idx = headerInfo.find(nomesNormalizados);
        if (idx == null) {
            return "";
        }
        Cell cell = row.getCell(idx);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private LocalDate detectarData(Row row, HeaderInfo headerInfo, String dataRaw) {
        Integer idx = headerInfo.find("startconstraint", "execstart", "actualexecutionstartdate");
        if (idx != null) {
            Cell cell = row.getCell(idx);
            LocalDate fromCell = parseDateCell(cell);
            if (fromCell != null) {
                return fromCell;
            }
        }
        return parseDateString(dataRaw);
    }

    private LocalDate parseDateCell(Cell cell) {
        if (cell == null) {
            return null;
        }

        CellType type = cell.getCellType();
        if (type == CellType.FORMULA) {
            type = cell.getCachedFormulaResultType();
        }

        if (type == CellType.NUMERIC) {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
            double value = cell.getNumericCellValue();
            if (value > 20000 && value < 60000) {
                return DateUtil.getLocalDateTime(value).toLocalDate();
            }
        }

        return parseDateString(formatter.formatCellValue(cell));
    }

    private LocalDate parseDateString(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }

        String normal = texto.trim();
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(normal, format);
            } catch (DateTimeParseException ignored) {
                // Continue trying known date formats.
            }
        }

        Matcher matcher = DATE_PATTERN.matcher(normal);
        if (matcher.find()) {
            String candidate = matcher.group(1);
            for (DateTimeFormatter format : DATE_FORMATS) {
                try {
                    return LocalDate.parse(candidate, format);
                } catch (DateTimeParseException ignored) {
                    // Continue trying known date formats.
                }
            }
        }

        return null;
    }

    private TipoManutencao detectarTipo(String... textos) {
        String full = normalizarCompacto(String.join(" ", textos));
        if (full.contains("instrument") || full.contains("instr")) {
            return TipoManutencao.INSTRUMENTACAO;
        }
        if (full.contains("eletric") || full.contains("eltr")) {
            return TipoManutencao.ELETRICA;
        }
        if (full.contains("caldeir")) {
            return TipoManutencao.CALDEIRARIA;
        }
        if (full.contains("pint")) {
            return TipoManutencao.PINTURA;
        }
        if (full.contains("andaim")) {
            return TipoManutencao.ANDAIME;
        }
        if (full.contains("mec") || full.contains("mecan")) {
            return TipoManutencao.MECANICA;
        }
        return null;
    }

    private String detectarArea(List<String> areasConhecidas, String... textos) {
        String full = normalizarBusca(String.join(" ", textos));
        for (String area : areasConhecidas) {
            String areaNorm = normalizarBusca(area).trim();
            if (areaNorm.isBlank()) {
                continue;
            }

            Pattern p = Pattern.compile("(^|[^A-Z0-9])" + Pattern.quote(areaNorm) + "($|[^A-Z0-9])");
            if (p.matcher(full).find()) {
                return area;
            }
        }
        return null;
    }

    private static String normalizarCompacto(String valor) {
        if (valor == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "");
    }

    private static String normalizarBusca(String valor) {
        if (valor == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(valor, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", " ").trim();
    }

    private static boolean todosVazios(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private static String primeiroNaoVazio(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return "";
    }

    private static String nuloSeVazio(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }

    private static final class HeaderInfo {
        private final int rowIndex;
        private final Map<String, Integer> indices;

        private HeaderInfo(int rowIndex, Map<String, Integer> indices) {
            this.rowIndex = rowIndex;
            this.indices = indices;
        }

        private Integer find(String... keys) {
            for (String key : keys) {
                Integer idx = indices.get(key);
                if (idx != null) {
                    return idx;
                }
            }
            return null;
        }
    }
}
