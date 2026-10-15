package com.medic.etl.service.schedule.job;

import com.medic.etl.model.schedule.ScheduleJob;

public interface Job {

    ScheduleJob getJob();
    void run();
}
