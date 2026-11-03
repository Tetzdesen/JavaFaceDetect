package br.com.javafacedetect.api.dto;

public record ReconhecimentoResponse(boolean reconhecido, Long pessoaId, String nome, Double distancia) {

    public static ReconhecimentoResponse naoReconhecido() {
        return new ReconhecimentoResponse(false, null, null, null);
    }

    public static ReconhecimentoResponse reconhecido(Long pessoaId, String nome, double distancia) {
        return new ReconhecimentoResponse(true, pessoaId, nome, distancia);
    }
}
