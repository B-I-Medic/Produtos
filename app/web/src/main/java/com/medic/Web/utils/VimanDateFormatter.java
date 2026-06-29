package com.medic.Web.utils;

import java.time.LocalDate;

public class VimanDateFormatter {

    public static String formatterToViman(LocalDate data) {

        return String.format("%04d%02d%02d", data.getYear(), data.getMonthValue(), data.getDayOfMonth());
    }
}
