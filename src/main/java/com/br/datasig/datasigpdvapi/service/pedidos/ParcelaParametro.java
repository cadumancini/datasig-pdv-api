package com.br.datasig.datasigpdvapi.service.pedidos;

import com.br.datasig.datasigpdvapi.entity.PagamentoPedido;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@AllArgsConstructor
@Getter
public class ParcelaParametro {
    private final String vlrPar;
    private final String vlrMaior;

    public static ParcelaParametro definirValorParcela(PagamentoPedido pagto) {
        double valorParcela = pagto.getValorPago() / pagto.getCondicao().getQtdParCpg();
        BigDecimal bdVlr = toRoundedBigDecimal(valorParcela);

        BigDecimal bdVlrMaior = calcValorMaior(pagto, bdVlr);

        String vlrParStr = PedidoUtils.toFormattedString(bdVlr);
        String vlrMaiorStr = PedidoUtils.toFormattedString(bdVlrMaior);
        return new ParcelaParametro(vlrParStr, vlrMaiorStr);
    }

    private static BigDecimal calcValorMaior(PagamentoPedido pagto, BigDecimal bdVlr) {
        BigDecimal valueToSubtract = bdVlr.multiply(BigDecimal.valueOf(pagto.getCondicao().getQtdParCpg()));
        BigDecimal vlrRestante = pagto.getCondicao().getQtdParCpg() == 1 ?
                BigDecimal.valueOf(0) :
                BigDecimal.valueOf(pagto.getValorPago())
                        .subtract(valueToSubtract);
        return bdVlr.add(vlrRestante);
    }

    private static BigDecimal toRoundedBigDecimal(double percentualParcela) {
        BigDecimal bdPrc = BigDecimal.valueOf(percentualParcela);
        bdPrc = bdPrc.setScale(2, RoundingMode.FLOOR);
        return bdPrc;
    }
}