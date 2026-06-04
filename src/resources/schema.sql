DROP TABLE IF EXISTS tranzactii;
DROP TABLE IF EXISTS carduri;
DROP TABLE IF EXISTS conturi;
DROP TABLE IF EXISTS clienti_premium;
DROP TABLE IF EXISTS clienti;

CREATE TABLE clienti (
    id_client          INT          PRIMARY KEY,
    cnp                VARCHAR(13)  NOT NULL UNIQUE,
    nume               VARCHAR(100) NOT NULL,
    prenume            VARCHAR(100) NOT NULL,
    adresa             VARCHAR(255),
    data_nastere       DATE,
    data_inregistrare  DATE,
    status_client      VARCHAR(20)  DEFAULT 'ACTIV',
    tip_client         VARCHAR(20)  DEFAULT 'NORMAL'
);

CREATE TABLE clienti_premium (
    cnp                        VARCHAR(13) PRIMARY KEY,
    limita_maxima_tranzactii   DOUBLE      DEFAULT 50000.0,
    dobanda_bonus              DOUBLE      DEFAULT 0.75,
    acces_credit_rapid         BOOLEAN     DEFAULT TRUE,
    FOREIGN KEY (cnp) REFERENCES clienti(cnp) ON DELETE CASCADE
);


CREATE TABLE conturi (
    iban             VARCHAR(30)  PRIMARY KEY,
    sold             DOUBLE       DEFAULT 0.0,
    valuta           VARCHAR(10)  DEFAULT 'RON',
    data_deschidere  DATE,
    cnp_titular      VARCHAR(13)  NOT NULL,
    FOREIGN KEY (cnp_titular) REFERENCES clienti(cnp) ON DELETE CASCADE
);

CREATE TABLE carduri (
    numar_card     VARCHAR(20) PRIMARY KEY,
    pin            VARCHAR(10) NOT NULL,
    data_expirare  DATE,
    tip_card       VARCHAR(10),
    iban_cont      VARCHAR(30) NOT NULL,
    blocat         BOOLEAN     DEFAULT FALSE,
    FOREIGN KEY (iban_cont) REFERENCES conturi(iban) ON DELETE CASCADE
);

CREATE TABLE tranzactii (
    id_tranzactie   VARCHAR(50)  PRIMARY KEY,
    suma            DOUBLE       NOT NULL,
    data_si_ora     DATETIME     NOT NULL,
    descriere       VARCHAR(255),
    tip_tranzactie  VARCHAR(20)  NOT NULL,
    iban_sursa      VARCHAR(30),
    iban_destinatar VARCHAR(30),
    FOREIGN KEY (iban_sursa)      REFERENCES conturi(iban) ON DELETE SET NULL,
    FOREIGN KEY (iban_destinatar) REFERENCES conturi(iban) ON DELETE SET NULL
);
