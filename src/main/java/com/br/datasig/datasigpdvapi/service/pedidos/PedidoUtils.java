package com.br.datasig.datasigpdvapi.service.pedidos;

import com.br.datasig.datasigpdvapi.entity.PagamentoPedido;
import com.br.datasig.datasigpdvapi.entity.Parcela;
import com.br.datasig.datasigpdvapi.token.TokensManager;

import java.math.BigDecimal;
import java.util.*;

public class PedidoUtils {
    public static String normalizeQtdPed(String vlr) {
        return vlr.replace(".",",");
    }

    public static Date definirDataParcela(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    public static String toFormattedString(BigDecimal bdPrc) {
        return String.format("%.2f", bdPrc).replace(".", ",");
    }

    public static String getVlrPar(ParcelaParametro parcelaParametro, int seqPar, PagamentoPedido pagto, Parcela parcela) {
        if (pagto.getCondicao().getTipPar().equals("1")) {
            if (seqPar == 1) return parcelaParametro.getVlrMaior();
            else return parcelaParametro.getVlrPar();
        } else if (pagto.getCondicao().getTipPar().equals("2")) {
            if (seqPar == pagto.getCondicao().getQtdParCpg()) return parcelaParametro.getVlrMaior();
            else return parcelaParametro.getVlrPar();
        }
        return calcVlrPerc(pagto, parcela);
    }

    private static String calcVlrPerc(PagamentoPedido pagto, Parcela parcela) {
        double perParDouble = Double.parseDouble(parcela.getPerPar().replace(",", "."));
        BigDecimal perPar = BigDecimal.valueOf(perParDouble);
        BigDecimal vlrPago = BigDecimal.valueOf(pagto.getValorPago());
        BigDecimal vlrPar = perPar.multiply(vlrPago).divide(BigDecimal.valueOf(100));
        return PedidoUtils.toFormattedString(vlrPar);
    }

    public static void ajustarValores(List<HashMap<String, Object>> lista, double valorPago) {
        // Convert "vlrPar" strings to doubles
        List<Double> valores = new ArrayList<>();
        for (Map<String, Object> map : lista) {
            String valorStr = (String) map.get("vlrPar");
            double valor = Double.parseDouble(valorStr.replace(",", "."));
            valores.add(valor);
        }

        // Calculate sum
        double soma = valores.stream().mapToDouble(Double::doubleValue).sum();

        // Difference
        double diff = valorPago - soma;

        if (Math.abs(diff) > 0.00001) { // If adjustment needed
            // Find index of max value
            int idxMax = 0;
            for (int i = 1; i < valores.size(); i++) {
                if (valores.get(i) > valores.get(idxMax)) {
                    idxMax = i;
                }
            }

            // Adjust the highest value
            double novoValor = valores.get(idxMax) + diff;
            valores.set(idxMax, novoValor);

            // Update back in the list (converting dot back to comma)
            for (int i = 0; i < lista.size(); i++) {
                String formatted = String.format(Locale.US, "%.2f", valores.get(i)).replace(".", ",");
                lista.get(i).put("vlrPar", formatted);
            }
        }
    }

    public static void orderParcelas(List<HashMap<String, Object>> parcelas) {
        parcelas.sort((map1, map2) -> {
            // Comparar por data
            var data1 = (Comparable) map1.get("vctDat");
            var data2 = (Comparable) map2.get("vctDat");

            int result = data1.compareTo(data2);

            // Se forem iguais, comparar por foma de pagto.
            if (result == 0) {
                var fpg1 = (Comparable) map1.get("codFpg");
                var fpg2 = (Comparable) map2.get("codFpg");
                result = fpg1.compareTo(fpg2);
            }

            return result;
        });

        // Redefinir seqPar
        int seqPar = 0;
        for (HashMap<String, Object> parcela : parcelas) {
            seqPar++;
            parcela.put("seqPar", String.valueOf(seqPar));
            parcela.put("numPar", String.valueOf(seqPar));
        }
    }

    public static String formatValue(String vlr) {
        if (vlr == null) return "0,00";
        return vlr.trim().isEmpty() ? "0,0" : vlr.replace(".","");
    }

    public static String definirCodCli(String codCli, String token) {
        if (codCli == null || codCli.isEmpty())
            codCli = TokensManager.getInstance().getParamsPDVFromToken(token).getCodCli();

        return codCli;
    }
}
