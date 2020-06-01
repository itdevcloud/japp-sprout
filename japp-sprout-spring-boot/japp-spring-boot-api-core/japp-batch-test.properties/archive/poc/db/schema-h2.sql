/*==============================================================*/
/* Table: REGION                                              */
/*==============================================================*/
create table REGION
(
   ID                   INT NOT NULL AUTOINCREMENT,
   REGION_NAME          VARCHAR(20),
   CREATED_BY           VARCHAR(30),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR(30),
   UPDATED_DATE         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT REGN_PK PRIMARY KEY ( ID )
);

/*==============================================================*/
/* Table: DISTRICT                                              */
/*==============================================================*/
create table DISTRICT(
   ID                   INT NOT NULL AUTO_INCREMENT,
   DISTRICT_NUM         INT,
   DISTRICT_NAME        VARCHAR(20),
   REGION_ID            INT NOT NULL,
   CREATED_BY           VARCHAR(30),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR(30),
   UPDATED_DATE         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT DIST_PK PRIMARY KEY ( ID )
);
ALTER TABLE DISTRICT ADD CONSTRAINT REG_DIST_FK FOREIGN KEY
  (
    REGION_ID
  )
  REFERENCES REGION
  (
    ID
  );

/*==============================================================*/
/* Table: ADDRESS                                              */
/*==============================================================*/
create table ADDRESS  (
   ID                   INT NOT NULL AUTO_INCREMENT,
   ADDRESS_LINE         VARCHAR(50),
   MUNICIPALITY         VARCHAR(20),
   PROVINCE             VARCHAR(20),
   POST_CODE            VARCHAR(7),
   CREATED_BY           VARCHAR(30),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR(30),
   UPDATED_DATE         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT ADDR_PK PRIMARY KEY ( ID )
);

/*==============================================================*/
/* Table: CONTACT                                              */
/*==============================================================*/
create table CONTACT  (
   ID                   INT NOT NULL AUTO_INCREMENT,
   FIRST_NAME           VARCHAR(15),
   MIDDLE_NAME          VARCHAR(15),
   LAST_NAME            VARCHAR(15),
   EMAIL                VARCHAR(30),
   PHONE                VARCHAR(20),
   FAX                  VARCHAR(20),
   ADDRESS_ID           INT,
   CREATED_BY           VARCHAR(30),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR(30),
   UPDATED_DATE         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT CONT_PK PRIMARY KEY ( ID )
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
   ID                   INT NOT NULL AUTO_INCREMENT,
   CVOR                 VARCHAR(9),
   LEGAL_NAME           VARCHAR(50),
   DESCRIPTION          VARCHAR(100),
   STATUS_ID            INT,
   CONTACT_ID           INT,
   CREATED_BY           VARCHAR(30),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR(30),
   UPDATED_DATE         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   END_REASON           VARCHAR(30),
   END_DATE             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT OPER_PK PRIMARY KEY ( ID )
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
   ID                   INT NOT NULL AUTO_INCREMENT,
   `NAME`				VARCHAR(30),
   OPERATOR_ID          INT NOT NULL,
   DESCRIPTION          VARCHAR(100),
   DISTRICT_ID          INT,
   STATUS_ID            INT,
   CONTACT_ID           INT,
   ADDRESS_ID           INT,
   TOTAL_VEHICLES       INT DEFAULT 0,
   SIZE_CAT1     INT DEFAULT 0,
   SIZE_CAT2     INT DEFAULT 0,
   SAMPLE_SIZE_CAT1     INT DEFAULT 0,
   SAMPLE_SIZE_CAT2     INT DEFAULT 0,
   CREATED_BY           VARCHAR(30),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR(30),
   UPDATED_DATE         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   END_REASON           VARCHAR(30),
   END_DATE             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT TERM_PK PRIMARY KEY ( ID )
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
   ID                   INT NOT NULL AUTO_INCREMENT,
   TERMINAL_ID          INT NOT NULL,
   STATUS_ID            INT,
   OFFICER_ID           INT,
   INSPECTION_SCHEDULE_DATE DATE,
   INSPECTION_DUE_DATE DATE,
   INSPECTION_DATE DATE,
   LAST_INSPECTION_ID INT,
   TOTAL_DEFECTS INT,
   TOTAL_OOS INT,
   RANK VARCHAR(1),
   CREATED_BY VARCHAR(30),
   CREATED_DATE TIMESTAMP,
   UPDATED_BY VARCHAR(30),
   UPDATED_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   END_REASON VARCHAR(30),
   END_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

   CONSTRAINT INSP_SCH_PK PRIMARY KEY ( ID )
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
   ID                   INT NOT NULL AUTO_INCREMENT,
   VIN                  VARCHAR(20),
   PLATE                VARCHAR(10),
   VEHICLE_TYPE         VARCHAR(10),
   MAKE                 VARCHAR(10),
   MODEL                VARCHAR(10),
   COLOUR               VARCHAR(10),
   VEHICLE_YEAR         INT,
   STATUS_ID            INT,
   TERMINAL_ID          INT,
   CVOR_NUMBER          VARCHAR(9),
   LAST_SYCH_DATE       TIMESTAMP,
   CREATED_BY           VARCHAR(30),
   CREATED_DATE         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   UPDATED_BY           VARCHAR(30),
   UPDATED_DATE         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   END_REASON           VARCHAR(30),
   END_DATE             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT VEH_PK PRIMARY KEY ( ID )
);

/*==============================================================*/
/* Table: VEH_INSPECTION_RESULT                                               */
/*==============================================================*/
create table VEH_INSPECTION_RESULT  (
   ID                   INT NOT NULL AUTO_INCREMENT,
   CVIR              	VARCHAR(30),
   VEHICLE_ID           INT NOT NULL,
   INSP_SCHEDULE_ID     INT NOT NULL,
   NUM_DEFECTS          INT,
   NUM_OOS              INT,
   INSP_CATEGORY_ID	    INT,
   INSPECTION_DATE      DATE,
   OFFICER_ID           INT,
   CREATED_BY           VARCHAR(30),
   CREATED_DATE         TIMESTAMP,
   UPDATED_BY           VARCHAR(30),
   UPDATED_DATE         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   END_REASON           VARCHAR(30),
   END_DATE             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT VEH_INSP_PK PRIMARY KEY ( ID )
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
	ID           INT NOT NULL AUTO_INCREMENT, 
	BADGE_NUM    INT NOT NULL, 
	OFFICER_NAME VARCHAR(50) NOT NULL, 
	EMAIL        VARCHAR(50), 
	CONSTRAINT ENFORCEMENT_OFFICER_PK PRIMARY KEY (ID)
);
