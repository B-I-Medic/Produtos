alter table produto
    alter column anvisa drop not null,
    alter column criad_em drop not null;

alter table produto
    rename column criad_em to criado_em;
