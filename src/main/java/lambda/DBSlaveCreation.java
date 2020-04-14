package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.CreateDBInstanceReadReplicaRequest;
import com.amazonaws.services.rds.model.DBInstance;

import java.util.Map;

//Step 4
public class DBSlaveCreation implements RequestHandler<Map<String, Object>, Object>
{
    @Override
    public Object handleRequest(Map<String, Object> input, Context context) {

        AmazonRDS client = AmazonRDSClient.builder().build();

        System.out.println(input);

        String dbInstanceIdentifier = (String) input.get("DBInstanceIdentifier");

        DBInstance dbInstanceReadReplica =
                client.createDBInstanceReadReplica(
                        new CreateDBInstanceReadReplicaRequest()
                                .withSourceDBInstanceIdentifier(dbInstanceIdentifier)
                                .withDBInstanceIdentifier(dbInstanceIdentifier + "slave")
                );

        client.shutdown();

        return dbInstanceReadReplica;
    }
}