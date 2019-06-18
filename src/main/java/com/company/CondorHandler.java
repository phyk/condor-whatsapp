package com.company;

import condor.api.CondorApi;
import condor.api.contentprocessor.Oscillation;
import condor.contentprocessor.annotation.turntaking.TimeUnit;
import condor.data.contract.db.entities.IDataset;

public class CondorHandler {
    public void connectToCondor(String licenseKey, String host, String port, String username, String password,
                                String database, String linkCsv, String actorCsv, String linkExport, String actorExport)
    {
        CondorApi condor = new CondorApi(licenseKey, null);

        condor.connectToDataBase(host, port, username, password, database);
        String datasetname = "myMessages";

        IDataset iDataset = condor.setOrCreateDataset(datasetname);
        condor.importCSV(actorCsv, linkCsv, iDataset, ";");
        condor.openDateBase(iDataset);
        condor.calcDegree(false);
        condor.calcBetweenness(false);
        condor.calcOscillation(Oscillation.MeasureType.BETWEENNESS, "Days", 1, 3, 0.5);
        condor.calcContribution();
        condor.calcTurnTaking(0, TimeUnit.DAYS, 4, TimeUnit.DAYS, false, false);
        // Try it out with "" as language for automatic detection
        condor.calcSentiment("German", true, "content", null, true, true, false);

        condor.calcCommunity();
        condor.exportActors(actorExport, ";", null);
        condor.exportEdges(linkExport, ";");

        // TODO Filter the Export Strings for personal information
        String fileActor = condor.exportActors(";", null);
        String file = condor.exportEdges(";");

        condor.unloadDatasets();
        condor.deleteDataset(iDataset);
        System.exit(0);
    }

    public static void calculateHonestSignals(String licenseKey, String host, String port, String username, String password,
                                              String database, String linkCsv, String actorCsv, String linkExport,
                                              String actorExport)
    {
        new CondorHandler().connectToCondor
                (licenseKey,host, port, username, password, database, linkCsv, actorCsv, linkExport, actorExport);
    }
}
