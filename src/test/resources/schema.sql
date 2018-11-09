create table T_SAMORDNINGSPLIKTIG_VEDTAK
(
  id bigint auto_increment,
  ytelsesType varchar(64) not null,
  identifikator char(11) not null,
  vedtakId varchar(64) not null,
  fom date not null,
  tom date,
  primary key(id)
);