package com.medic.ETL.service.schedule.job;

import com.medic.ETL.model.schedule.ScheduleJob;

public interface Job {

    ScheduleJob getJob();
    void run();
}
