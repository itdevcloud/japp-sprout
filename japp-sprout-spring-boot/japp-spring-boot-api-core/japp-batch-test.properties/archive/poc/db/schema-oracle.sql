drop table REGION cascade constraints;
drop table DISTRICT cascade constraints;
drop table ADDRESS cascade constraints;
drop table CONTACT cascade constraints;
drop table VEH_INSPECTION_RESULT cascade constraints;
drop table VEHICLE cascade constraints;
drop table INSPECTION_SCHEDULE cascade constraints;
drop table TERMINAL cascade constraints;
drop table OPERATOR cascade constraints;
drop table ENFORCEMENT_OFFICER cascade constraints;

drop SEQUENCE BITS_ADDRESS_ID_SEQ;
drop SEQUENCE BITS_CONTACT_ID_SEQ;
drop SEQUENCE BITS_TERMINAL_ID_SEQ;
drop SEQUENCE BITS_OPERATOR_ID_SEQ;
drop SEQUENCE BITS_INSP_SCH_ID_SEQ;
drop SEQUENCE BITS_VEHICLE_ID_SEQ;
drop SEQUENCE BITS_VEH_INSP_ID_SEQ;
drop SEQUENCE BITS_ENF_OFF_ID_SEQ;

/*==============================================================*/
/* Table: REGION                                              */
/*==============================================================*/
create table REGION
(
   ID                   NUMBER NOT NULL,
   REGION_NAME          VARCHAR2(20 BYTE),
   CREATED_BY           VARCHAR2(30 BYTE),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR2(30 BYTE),
   UPDATED_DATE         TIMESTAMP,
   CONSTRAINT REGN_PK PRIMARY KEY ( ID ) ENABLE
);
insert into REGION(ID, REGION_NAME)
VALUES (1, 'Central');
insert into REGION(ID, REGION_NAME)
VALUES (2, 'Eastern');
insert into REGION(ID, REGION_NAME)
VALUES (3, 'Head Office');
insert into REGION(ID, REGION_NAME)
VALUES (4, 'Northern');
insert into REGION(ID, REGION_NAME)
VALUES (5, 'Southwestern');

/*==============================================================*/
/* Table: DISTRICT                                              */
/*==============================================================*/
create table DISTRICT(
   ID                   NUMBER NOT NULL,
   DISTRICT_NUM         NUMBER,
   DISTRICT_NAME        VARCHAR2(20 BYTE),
   REGION_ID            NUMBER NOT NULL,
   CREATED_BY           VARCHAR2(30 BYTE),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR2(30 BYTE),
   UPDATED_DATE         TIMESTAMP,
   CONSTRAINT DIST_PK PRIMARY KEY ( ID ) ENABLE
);
ALTER TABLE DISTRICT ADD CONSTRAINT REG_DIST_FK FOREIGN KEY
  (
    REGION_ID
  )
  REFERENCES REGION
  (
    ID
  );
insert into DISTRICT(ID, DISTRICT_NUM, DISTRICT_NAME, REGION_ID)
values (1, 10, 'Head Office', 3);
insert into DISTRICT(ID, DISTRICT_NUM, DISTRICT_NAME, REGION_ID)
values (2, 21, 'Metro Toronto', 1);
insert into DISTRICT(ID, DISTRICT_NUM, DISTRICT_NAME, REGION_ID)
values (3, 10, 'Kingston', 2);
insert into DISTRICT(ID, DISTRICT_NUM, DISTRICT_NAME, REGION_ID)
values (4, 10, 'London', 5);
insert into DISTRICT(ID, DISTRICT_NUM, DISTRICT_NAME, REGION_ID)
values (5, 10, 'North Bay', 4);

/*==============================================================*/
/* Table: ADDRESS                                              */
/*==============================================================*/
create table ADDRESS  (
   ID                   NUMBER NOT NULL,
   ADDRESS_LINE         VARCHAR2(50 BYTE),
   MUNICIPALITY         VARCHAR2(20 BYTE),
   PROVINCE             VARCHAR2(20 BYTE),
   POST_CODE            VARCHAR2(7 BYTE),
   CREATED_BY           VARCHAR2(30 BYTE),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR2(30 BYTE),
   UPDATED_DATE         TIMESTAMP,
   CONSTRAINT ADDR_PK PRIMARY KEY ( ID ) ENABLE
);

/*==============================================================*/
/* Table: CONTACT                                              */
/*==============================================================*/
create table CONTACT  (
   ID                   NUMBER NOT NULL,
   FIRST_NAME           VARCHAR2(15 BYTE),
   MIDDLE_NAME           VARCHAR2(15 BYTE),
   LAST_NAME            VARCHAR2(15 BYTE),
   EMAIL                VARCHAR2(30 BYTE),
   PHONE                VARCHAR2(20 BYTE),
   FAX                  VARCHAR2(20 BYTE),
   ADDRESS_ID           NUMBER,
   CREATED_BY           VARCHAR2(30 BYTE),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR2(30 BYTE),
   UPDATED_DATE         TIMESTAMP,
   CONSTRAINT CONT_PK PRIMARY KEY ( ID ) ENABLE
);
ALTER TABLE CONTACT ADD CONSTRAINT CONT_ADDR_FK FOREIGN KEY
  (
    ADDRESS_ID
  )
  REFERENCES ADDRESS
  (
    ID
  );
  
/*==============================================================*/
/* Table: OPERATOR                                              */
/*==============================================================*/
create table OPERATOR  (
   ID                   NUMBER NOT NULL,
   CVOR                 VARCHAR2(9 BYTE),
   LEGAL_NAME           VARCHAR2(50 BYTE),
   DESCRIPTION          VARCHAR2(100 BYTE),
   STATUS_ID            NUMBER,
   CONTACT_ID           NUMBER,
   CREATED_BY           VARCHAR2(30 BYTE),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR2(30 BYTE),
   UPDATED_DATE         TIMESTAMP,
   END_REASON           VARCHAR2(30 BYTE),
   END_DATE             TIMESTAMP,
   CONSTRAINT OPER_PK PRIMARY KEY ( ID ) ENABLE
);
ALTER TABLE OPERATOR ADD CONSTRAINT OPER_CONT_FK FOREIGN KEY
  (
    CONTACT_ID
  )
  REFERENCES CONTACT
  (
    ID
  );
/*==============================================================*/
/* Table: TERMINAL                                               */
/*==============================================================*/
create table TERMINAL  (
   ID                   NUMBER NOT NULL,
   NAME					VARCHAR2(30 BYTE),
   OPERATOR_ID          NUMBER NOT NULL,
   DESCRIPTION          VARCHAR2(100 BYTE),
   DISTRICT_ID          NUMBER,
   STATUS_ID            NUMBER,
   CONTACT_ID           NUMBER,
   ADDRESS_ID           NUMBER,
   TOTAL_VEHICLES       NUMBER DEFAULT 0,
   SIZE_CAT1     NUMBER DEFAULT 0,
   SIZE_CAT2     NUMBER DEFAULT 0,
   SAMPLE_SIZE_CAT1     NUMBER DEFAULT 0,
   SAMPLE_SIZE_CAT2     NUMBER DEFAULT 0,
   CREATED_BY           VARCHAR2(30 BYTE),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR2(30 BYTE),
   UPDATED_DATE         TIMESTAMP,
   END_REASON           VARCHAR2(30 BYTE),
   END_DATE             TIMESTAMP,
   CONSTRAINT TERM_PK PRIMARY KEY ( ID ) ENABLE
);
ALTER TABLE TERMINAL ADD CONSTRAINT TERM_OPER_FK FOREIGN KEY
  (
    OPERATOR_ID
  )
  REFERENCES OPERATOR
  (
    ID
  );
ALTER TABLE TERMINAL ADD CONSTRAINT TERM_CONT_FK FOREIGN KEY
  (
    CONTACT_ID
  )
  REFERENCES CONTACT
  (
    ID
  );
ALTER TABLE TERMINAL ADD CONSTRAINT TERM_ADDR_FK FOREIGN KEY
  (
    ADDRESS_ID
  )
  REFERENCES ADDRESS
  (
    ID
  );
ALTER TABLE TERMINAL ADD CONSTRAINT TERM_DIST_FK FOREIGN KEY
  (
    DISTRICT_ID
  )
  REFERENCES DISTRICT
  (
    ID
  );  
/*==============================================================*/
/* Table: INSPECTION_SCHEDULE                                               */
/*==============================================================*/
create table INSPECTION_SCHEDULE   (
   ID                   NUMBER NOT NULL,
   TERMINAL_ID          NUMBER NOT NULL,
   STATUS_ID            NUMBER,
   OFFICER_ID           NUMBER,
   INSPECTION_SCHEDULE_DATE	DATE,
   INSPECTION_DUE_DATE	DATE,
   INSPECTION_DATE		DATE,
   LAST_INSPECTION_ID	NUMBER,
   TOTAL_DEFECTS		NUMBER,
   TOTAL_OOS			NUMBER,
   RANK					VARCHAR2(1 BYTE),
   CREATED_BY           VARCHAR2(30 BYTE),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR2(30 BYTE),
   UPDATED_DATE         TIMESTAMP,
   END_REASON           VARCHAR2(30 BYTE),
   END_DATE             TIMESTAMP,
   CONSTRAINT INSP_SCH_PK PRIMARY KEY ( ID ) ENABLE
);
ALTER TABLE INSPECTION_SCHEDULE ADD CONSTRAINT INSP_SCH_TERM_FK FOREIGN KEY
(
TERMINAL_ID
)
REFERENCES TERMINAL
(
ID
);
ALTER TABLE INSPECTION_SCHEDULE ADD CONSTRAINT LAST_INSP_SCH_FK FOREIGN KEY
(
LAST_INSPECTION_ID
)
REFERENCES INSPECTION_SCHEDULE
(
ID
);

/*==============================================================*/
/* Table: VEHICLE                                               */
/*==============================================================*/
create table VEHICLE  (
   ID                   NUMBER NOT NULL,
   VIN                  VARCHAR2(20),
   PLATE                VARCHAR2(10),
   VEHICLE_TYPE         VARCHAR2(10),
   MAKE                 VARCHAR2(10),
   MODEL                VARCHAR2(10),
   COLOUR               VARCHAR2(10),
   YEAR					NUMBER,
   STATUS_ID            NUMBER,
   TERMINAL_ID          NUMBER,
   CVOR_NUMBER          VARCHAR2(9 BYTE),
   LAST_SYCH_DATE       TIMESTAMP,
   CREATED_BY           VARCHAR2(30 BYTE),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR2(30 BYTE),
   UPDATED_DATE         TIMESTAMP,
   END_REASON           VARCHAR2(30 BYTE),
   END_DATE             TIMESTAMP,
   CONSTRAINT VEH_PK PRIMARY KEY ( ID ) ENABLE
);
/*ALTER TABLE VEHICLE ADD CONSTRAINT VEH_TERM_FK FOREIGN KEY
(
TERMINAL_ID
)
REFERENCES TERMINAL
(
ID
);*/

/*==============================================================*/
/* Table: VEH_INSPECTION_RESULT                                               */
/*==============================================================*/
create table VEH_INSPECTION_RESULT  (
   ID                   NUMBER NOT NULL,
   CVIR              	VARCHAR2(30 BYTE),
   VEHICLE_ID           NUMBER NOT NULL,
   INSP_SCHEDULE_ID		NUMBER NOT NULL,
   NUM_DEFECTS			NUMBER,
   NUM_OOS				NUMBER,
   INSP_CATEGORY_ID		NUMBER,
   INSPECTION_DATE		DATE,
   OFFICER_ID           NUMBER,
   CREATED_BY           VARCHAR2(30 BYTE),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR2(30 BYTE),
   UPDATED_DATE         TIMESTAMP,
   END_REASON           VARCHAR2(30 BYTE),
   END_DATE             TIMESTAMP,
   CONSTRAINT VEH_INSP_PK PRIMARY KEY ( ID ) ENABLE
);
ALTER TABLE VEH_INSPECTION_RESULT ADD CONSTRAINT INSP_VEH_FK FOREIGN KEY
(
VEHICLE_ID
)
REFERENCES VEHICLE
(
ID
);
ALTER TABLE VEH_INSPECTION_RESULT ADD CONSTRAINT VEH_INSP_SCH_FK FOREIGN KEY
(
INSP_SCHEDULE_ID
)
REFERENCES INSPECTION_SCHEDULE
(
ID
);


/*==============================================================*/
/* Table: ENFORCEMENT_OFFICER                                   */
/*==============================================================*/
CREATE TABLE ENFORCEMENT_OFFICER (	
	"ID" NUMBER NOT NULL, 
	"BADGE_NUM" NUMBER NOT NULL, 
	"OFFICER_NAME" VARCHAR2(50 BYTE) NOT NULL, 
	"EMAIL" VARCHAR2(50 BYTE), 
	 CONSTRAINT "ENFORCEMENT_OFFICER_PK" PRIMARY KEY ("ID")
);
 
CREATE SEQUENCE BITS_ADDRESS_ID_SEQ;
CREATE SEQUENCE BITS_CONTACT_ID_SEQ;
CREATE SEQUENCE BITS_TERMINAL_ID_SEQ;
CREATE SEQUENCE BITS_OPERATOR_ID_SEQ;
CREATE SEQUENCE BITS_INSP_SCH_ID_SEQ;
CREATE SEQUENCE BITS_VEHICLE_ID_SEQ;
CREATE SEQUENCE BITS_VEH_INSP_ID_SEQ;
CREATE SEQUENCE BITS_ENF_OFF_ID_SEQ;

