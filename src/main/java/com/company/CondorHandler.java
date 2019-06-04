package com.company;

import condor.api.CondorApi;
import condor.api.contentprocessor.Oscillation;
import condor.contentprocessor.annotation.turntaking.TimeUnit;
import condor.data.contract.db.entities.IDataset;

public class CondorHandler {
    public void connectToCondor(String linkcsv, String actorcsv)
    {
        CondorApi condor = new CondorApi("9oi160m0dk8oafa2kkcpgtlbqg", null);

        condor.connectToDataBase("localhost", "3306", "root", "OQhpmmqbKBDvvk6k8EP3", "condorTemp");
        String datasetname = "myMessages";

        IDataset iDataset = condor.setOrCreateDataset(datasetname);
        condor.importCSV(actorcsv, linkcsv, iDataset, ";");
        condor.openDateBase(iDataset);
        condor.calcDegree(false);
        condor.calcBetweenness(false);
        condor.calcOscillation(Oscillation.MeasureType.BETWEENNESS, "Days", 1, 3, 0.5);
        condor.calcContribution();
        condor.calcTurnTaking(0, TimeUnit.DAYS, 4, TimeUnit.DAYS, false, false);
        // Try it out with "" as language for automatic detection
        condor.calcSentiment("German", true, "content", null, true, true, false);

        condor.calcCommunity();
        condor.exportActors("export.csv", ";", null);
        String fileActor = condor.exportActors(";", null);
        String file = condor.exportEdges(";");
        condor.unloadDatasets();
        condor.deleteDataset(iDataset);
    }

    public static void main(String[]args)
    {
        new CondorHandler().connectToCondor("condor_import.csv", "actors.csv");
        System.out.println("Done");
        System.exit(0);
    }
}
