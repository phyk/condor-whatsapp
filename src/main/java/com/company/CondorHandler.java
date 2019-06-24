package com.company;

import condor.api.CondorApi;
import condor.api.contentprocessor.Oscillation;
import condor.contentprocessor.annotation.turntaking.TimeUnit;
import condor.data.contract.db.entities.IDataset;
import javafx.concurrent.Task;

import java.util.Date;

public class CondorHandler {

    public void connectToCondor(String host, String port, String username, String password,
                                String database, String linkCsv, String actorCsv, String linkExport, String actorExport,
                                ProcessHandler reportProgress)
    {
        CondorApi condor = new CondorApi();

        reportProgress.passMessage("Condor Api opened");
        //condor.connectToDataBase(host, port, username, password, database);
        condor.connectWithoutDatabase();
        String datasetname = "myMessages";

        reportProgress.passMessage("Connected to Database");

        IDataset iDataset = condor.setOrCreateDataset(datasetname);
        reportProgress.passMessage("Importing csv Files. This may take some time");
        condor.importCSV(actorCsv, linkCsv, iDataset, ";");

        reportProgress.passMessage("Import complete");

        condor.openDateBase(iDataset);
        reportProgress.passMessage("Calculate Degree Centrality");
        condor.calcDegree(false);
        reportProgress.passMessage("Calculated Degree Centrality");
        reportProgress.passMessage("Calculate Betweeness Centrality");
        condor.calcBetweenness(false);
        reportProgress.passMessage("Calculated Betweeness Centrality");
        reportProgress.passMessage("Calculate Betweeness Centrality Oscillation");
        condor.calcOscillation(Oscillation.MeasureType.BETWEENNESS, "Days", 1, 3, 0.5);
        reportProgress.passMessage("Calculated Betweeness Centrality Oscillation");
        reportProgress.passMessage("Calculate Contribution");
        condor.calcContribution();
        reportProgress.passMessage("Calculated Contribution");
        reportProgress.passMessage("Calculate Turn Taking");
        condor.calcTurnTaking(0, TimeUnit.DAYS, 4, TimeUnit.DAYS, false, false);
        reportProgress.passMessage("Calculated Turn Taking");
        // Try it out with "" as language for automatic detection
        reportProgress.passMessage("Calculate Sentiment");
        condor.calcSentiment("German", true, "content", null, true, true, false);
        reportProgress.passMessage("Calculated Contribution");

        condor.calcCommunity();
        reportProgress.passMessage("Calculated Communities");
        Date insert = new Date();
        insert.setTime(0);
        condor.calcInfluence("German", "content", insert);
        reportProgress.passMessage("Calculated Influence");
        condor.calcReach(3, true);
        reportProgress.passMessage("Calculated Reach");

        condor.exportActors(actorExport, ";", null);
        condor.exportEdges(linkExport, ";");

        reportProgress.passMessage("Export files generated");

        condor.unloadDatasets();
        condor.deleteDataset(iDataset);
        reportProgress.passMessage("Dataset deleted");

        return;
    }

    public static void calculateHonestSignals(String host, String port, String username, String password,
                                              String database, String linkCsv, String actorCsv, String linkExport,
                                              String actorExport, ProcessHandler reportProgress)
    {
        new CondorHandler().connectToCondor
                (host, port, username, password, database, linkCsv, actorCsv, linkExport, actorExport, reportProgress);
        reportProgress.passMessage("Condor complete");
        reportProgress.cancel();
        return;
    }
}
