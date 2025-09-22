package com.br.datasig.datasigpdvapi.service.pedidos;

import com.br.datasig.datasigpdvapi.entity.PayloadItemPedido;
import com.br.datasig.datasigpdvapi.entity.PayloadPedido;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DescontosProcessor {
    public static class ResultadoItem {
        private final String seqIpd;
        private final BigDecimal descontoTotal;
        private final String descontoTotalStr;

        public ResultadoItem(String seqIpd, BigDecimal descontoTotal, String descontoTotalStr) {
            this.seqIpd = seqIpd;
            this.descontoTotal = descontoTotal;
            this.descontoTotalStr = descontoTotalStr;
        }

        public String getSeqIpd() {
            return seqIpd;
        }

        public BigDecimal getDescontoTotal() {
            return descontoTotal;
        }

        public String getDescontoTotalStr() {
            return descontoTotalStr;
        }
    }

    public static Map<String, ResultadoItem> calcularDescontos(PayloadPedido pedido) {
        List<ResultadoItem> resultados = new ArrayList<>();

        BigDecimal vlrTotPedido = bd(pedido.getVlrTot());
        BigDecimal vlrDarPedido = bd(pedido.getVlrDar());

        if (vlrTotPedido.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor total do pedido deve ser maior que zero.");
        }

        BigDecimal percentualPedido = BigDecimal.ZERO;
        boolean aplicarRateio = vlrDarPedido.compareTo(BigDecimal.ZERO) > 0;
        if (aplicarRateio) {
            percentualPedido = vlrDarPedido.divide(vlrTotPedido, 10, RoundingMode.HALF_UP);
        }

        for (PayloadItemPedido item : pedido.getItens()) {
            BigDecimal vlrTotItem = bd(item.getVlrTot());

            // desconto já existente no item
            BigDecimal descontoExistente = BigDecimal.ZERO;
            if (notEmpty(item.getVlrDsc())) {
                descontoExistente = bd(item.getVlrDsc());
            } else if (notEmpty(item.getPerDsc())) {
                BigDecimal perDsc = bd(item.getPerDsc()).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                descontoExistente = vlrTotItem.multiply(perDsc);
            }

            BigDecimal descontoFinal;
            if (aplicarRateio) {
                // aplica rateio do pedido
                BigDecimal descontoRateado = vlrTotItem.multiply(percentualPedido);
                descontoFinal = descontoExistente.add(descontoRateado);
            } else {
                // sem rateio, mantém apenas o desconto existente
                descontoFinal = descontoExistente;
            }

            // arredonda para 2 casas decimais
            descontoFinal = descontoFinal.setScale(2, RoundingMode.HALF_UP);
            String descontoFinalStr = descontoFinal.toString()
                    .replace(",", "")
                    .replace(".", ",");

            resultados.add(new ResultadoItem(item.getSeqIpd(), descontoFinal, descontoFinalStr));
        }

        return resultados.stream()
                        .collect(Collectors.toMap(DescontosProcessor.ResultadoItem::getSeqIpd, r -> r));
    }

    private static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static BigDecimal bd(Double d) {
        return d == null ? BigDecimal.ZERO : BigDecimal.valueOf(d);
    }

    private static BigDecimal bd(String valor) {
        if (valor == null || valor.trim().isEmpty()) return BigDecimal.ZERO;
        valor = valor.trim().replace(",", ".");
        return new BigDecimal(valor);
    }
}
