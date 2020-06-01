/*==============================================================*/
/* Table: REGION                                              */
/*==============================================================*/
insert into REGION(ID, REGION_NAME) VALUES (1, 'Central');
insert into REGION(ID, REGION_NAME) VALUES (2, 'Eastern');
insert into REGION(ID, REGION_NAME) VALUES (3, 'Head Office');
insert into REGION(ID, REGION_NAME) VALUES (4, 'Northern');
insert into REGION(ID, REGION_NAME) VALUES (5, 'Southwestern');

/*==============================================================*/
/* Table: DISTRICT                                              */
/*==============================================================*/
insert into DISTRICT(ID, DISTRICT_NUM, DISTRICT_NAME, REGION_ID) values (1, 10, 'Head Office', 3);
insert into DISTRICT(ID, DISTRICT_NUM, DISTRICT_NAME, REGION_ID) values (2, 21, 'Metro Toronto', 1);
insert into DISTRICT(ID, DISTRICT_NUM, DISTRICT_NAME, REGION_ID) values (3, 10, 'Kingston', 2);
insert into DISTRICT(ID, DISTRICT_NUM, DISTRICT_NAME, REGION_ID) values (4, 10, 'London', 5);
insert into DISTRICT(ID, DISTRICT_NUM, DISTRICT_NAME, REGION_ID) values (5, 10, 'North Bay', 4);


/* Test data */
insert into OPERATOR (ID, CVOR, LEGAL_NAME) values (100, '1010101', 'Legal name....');
insert into TERMINAL (ID, NAME, OPERATOR_ID, DISTRICT_ID) values (100, 'final', 100, 1);
insert into INSPECTION_SCHEDULE (ID, TERMINAL_ID) values (100, 100);
insert into VEHICLE (ID, VIN, TERMINAL_ID) values (100, '876237461873673', 100);

