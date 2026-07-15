create table config_schedule (
    id uuid primary key,
    job text not null,
    cron text not null,
    ativo boolean not null,
    atualizado_por uuid not null,
    atualizado_em timestamptz not null
);

alter table config_schedule add constraint uq_config_schedule_entidade unique (job);

alter table config_schedule add constraint fk_config_schedule_atualizado_por foreign key (atualizado_por) references usuario (id);