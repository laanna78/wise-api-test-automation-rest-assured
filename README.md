## Wise REST API Test Automation Framework (REST Assured)
Ez a tárhely a szakdolgozatom gyakorlati részét képező, __Java-alapú__ API tesztautomatizálási keretrendszert tartalmazza. A projekt célja a __REST Assured__ keretrendszer hatékonyságának vizsgálata banki RESTful API tesztelésben.
### 📌 Projekt célkitűzések
A keretrendszer a __Wise (sandbox)__ API végpontjait teszteli, szimulálva egy teljes körű pénzügyi tranzakciós folyamatot a hitelesítéstől az utalás végrehajtásáig.  

A Wise API dokumentációja: [Wise API Docs](https://docs.wise.com)  
A Wise tesztkörnyezet elérhetősége: [Wise Sandbox](https://wise-sandbox.com)

### 🛠 Technológiai stack
* __Engine:__ TestNG

* __Build tool:__ Maven

* __Reporting:__ Allure Report (Standalone HTML)

* __Logs:__ Log4j2 / Jackson (JSON Szerializáció)

### 📂 Projektstruktúra és sorrendiség
A tesztek szigorú szekvenciális sorrendben futnak a banki üzleti logika integritásának biztosítása érdekében:

`01_security/`: Hitelesítési és jogosultsági tesztek (Bearer token, érvénytelen adatok).

`02_balance/`: Egyenleg lekérdezése és többdevizás számlák validálása.

`03_transaction/`: Tranzakciós előzmények és állapotok ellenőrzése.

`04_finance/`: Komplett utalási folyamat (Quote creation -> Recipient -> Transfer).

`05_foreignExchange/`: Árfolyam-kalkulációk és devizaváltási logika.

`06_errorHandling/`: Hibatűrő képesség és validációs üzenetek vizsgálata.

### 📊 Riportálás
A futtatás végén egy önálló (standalone) riport generálódik.
Ez a fájl bármilyen böngészőben megnyitható, és tartalmazza a tesztek státuszát, az időtartamokat, a request és response tartalmakat.

### 🚀 Futtatási útmutató
__Előfeltételek__
1. Java Development Kit (JDK) 17 vagy újabb

2. Apache Maven telepítve

__Telepítés__  

`mvn clean install`  

__Tesztek futtatása (CLI)__  

Az összes teszt futtatásához és az Allure riport elkészítéséhez:  
`mvn clean test`  
`mvn allure:report`  
`mvn allure:serve`  


