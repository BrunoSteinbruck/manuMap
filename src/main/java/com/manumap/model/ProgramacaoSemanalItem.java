package com.manumap.model;

public class ProgramacaoSemanalItem {

    private long id;
    private String ordem;
    private String descricao;
    private String atividade;
    private String local;
    private String centroPrincipal;
    private String centroExecucao;
    private String tipoAtividade;
    private String areaSugerida;
    private String dataProgramada;
    private Integer linhaExcel;
    private TipoManutencao tipoSugerido;
    private String origem;

    public ProgramacaoSemanalItem() {
    }

    public ProgramacaoSemanalItem(long id, String ordem, String descricao, String atividade, String local,
            String centroPrincipal, String centroExecucao, String tipoAtividade, String areaSugerida,
            String dataProgramada, Integer linhaExcel, TipoManutencao tipoSugerido, String origem) {
        this.id = id;
        this.ordem = ordem;
        this.descricao = descricao;
        this.atividade = atividade;
        this.local = local;
        this.centroPrincipal = centroPrincipal;
        this.centroExecucao = centroExecucao;
        this.tipoAtividade = tipoAtividade;
        this.areaSugerida = areaSugerida;
        this.dataProgramada = dataProgramada;
        this.linhaExcel = linhaExcel;
        this.tipoSugerido = tipoSugerido;
        this.origem = origem;
    }

    public long getId() {
        return id;
    }

    public String getOrdem() {
        return ordem;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getAtividade() {
        return atividade;
    }

    public String getLocal() {
        return local;
    }

    public String getCentroPrincipal() {
        return centroPrincipal;
    }

    public String getCentroExecucao() {
        return centroExecucao;
    }

    public String getTipoAtividade() {
        return tipoAtividade;
    }

    public String getAreaSugerida() {
        return areaSugerida;
    }

    public String getDataProgramada() {
        return dataProgramada;
    }

    public Integer getLinhaExcel() {
        return linhaExcel;
    }

    public TipoManutencao getTipoSugerido() {
        return tipoSugerido;
    }

    public String getOrigem() {
        return origem;
    }
}
