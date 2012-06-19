package uk.ac.imperial.evpool.db;

import org.apache.log4j.Logger;
import uk.ac.imperial.evpool.facts.Round;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CSVImport {

    private static final Logger logger = Logger
            .getLogger("uk.ac.imperial.evpool.db.CSVImport");


    public static Map<Integer,Double> importGridLoad(String filename,int roundStartPos, int timeStepsInHalfHour) {


        Map<Integer,Double> gridLoad = new HashMap<Integer,Double>();
        double loadMax = 0.0;
        double loadMin = Double.MAX_VALUE;

        try
        {
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);
            String stringRead = br.readLine();
            int lineCount = 1;
            while( (stringRead != null) && (lineCount <= 48) )
            {
                String[] elements = stringRead.split(",");

                if(elements.length < 2) {
                    throw new RuntimeException("line too short");
                }


                int period = Integer.parseInt(elements[0]);
                double load = Double.parseDouble(elements[1]);
                int round;
                // MOD((B31-30)*2,96)+1 - transform to round numbers from half hours
                if (period-roundStartPos < 0 )  {
                    round = (48*timeStepsInHalfHour-Math.abs(period - roundStartPos)*timeStepsInHalfHour)+1;
                } else {
                    round = (((period-roundStartPos)*timeStepsInHalfHour) % (48*timeStepsInHalfHour))+1;
                }
                logger.debug("period:"+period+" round:"+round+" timeStep:"+timeStepsInHalfHour+" period-roundstartpos:"+(period-roundStartPos));
                for (int a = 0;a < timeStepsInHalfHour; a++)  {
                    gridLoad.put(round+a,load);
                }

                if (load > loadMax) {
                    loadMax = load;
                }
                if (load < loadMin) {
                    loadMin = load;
                }

                stringRead = br.readLine();
                lineCount++;
            }
            br.close( );
        }
        catch(IOException ioe){}
        logger.debug("loadMax:"+loadMax+" loadMin:"+loadMin);
        gridLoad.put(-1,loadMax);
        gridLoad.put(-2,loadMin);
        return gridLoad;
    }
}
