package com.company;

import condor.api.CondorApi;
import condor.data.contract.db.entities.IDataset;

public class CondorHandler {
    public void connectToCondor()
    {
        CondorApi condor = new CondorApi("jt7455fcff3uiDGFDGACGC5", null);



        condor.connectToDataBase("localhost", "3306", "user", "PASSWORD", "testev");
        String datasetname = "tree34";
        for (IDataset d : condor.getAvailableDatasetsFromDatabase()) {
            System.out.println(d.getName());
        }
        IDataset iDataset = condor.setOrCreateDataset(datasetname);
        System.out.println(iDataset.getName());
        condor.wikiEvolution("Thomas_H._Malone", "ENGLISH", false, null, null, 2, true, "Do not fetch content");
        condor.openDateBase(iDataset);
        System.out.println(condor.getNumberOfNodes());
        condor.calcDegree(false);
        condor.calcReach(2, false);
        condor.calcCommunity();
        condor.exportActors("actors.csv", "|", null);
    }
}
