package com.kdrama.backend.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;

import com.kdrama.backend.enums.KRReleaseSchedule;

public class KRReleaseScheduleChecker {
    public static KRReleaseSchedule checkSchedule(ArrayList<String> airdates) {
        KRReleaseSchedule schedule = null;
        LocalDate[] dates = new LocalDate[airdates.size()];
        DayOfWeek[] dayOfWeeks = new DayOfWeek[airdates.size()];
        HashSet<DayOfWeek> airdateDayOfWeeks = new HashSet<DayOfWeek>();
        
        if (airdates == null || airdates.size() == 0) {
            return KRReleaseSchedule.UNKNOWN;
        }
        
        for (int i = 0; i < airdates.size(); i++) {
            dates[i] = LocalDate.parse(airdates.get(i));
            dayOfWeeks[i] = dates[i].getDayOfWeek();
            airdateDayOfWeeks.add(dayOfWeeks[i]);
        }

        // Determine if it is FULL_RELEASE based on the airdates of the first ep and the last ep, or if it is an one-episode drama
        if (dates[0].equals(dates[dates.length - 1]) || airdates.size() == 1) {
            schedule = KRReleaseSchedule.FULL_RELEASE;
        }
        // Determine other schedules
        else {
            if (airdateDayOfWeeks.size() == 2) {
                if (airdateDayOfWeeks.contains(DayOfWeek.MONDAY) && airdateDayOfWeeks.contains(DayOfWeek.TUESDAY)) {
                    schedule = KRReleaseSchedule.MON_TUE;
                }
                else if (airdateDayOfWeeks.contains(DayOfWeek.WEDNESDAY) && airdateDayOfWeeks.contains(DayOfWeek.THURSDAY)) {
                    schedule = KRReleaseSchedule.WED_THU;
                }
                else if (airdateDayOfWeeks.contains(DayOfWeek.FRIDAY) && airdateDayOfWeeks.contains(DayOfWeek.SATURDAY)) {
                    schedule = KRReleaseSchedule.FRI_SAT;
                }
                else if (airdateDayOfWeeks.contains(DayOfWeek.SATURDAY) && airdateDayOfWeeks.contains(DayOfWeek.SUNDAY)) {
                    schedule = KRReleaseSchedule.SAT_SUN;
                }
                else {
                    schedule = KRReleaseSchedule.OTHERS;
                }
            }
            else {
                schedule = KRReleaseSchedule.OTHERS;
            }
            
        }

        return schedule;
    }
}
