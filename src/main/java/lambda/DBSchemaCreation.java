package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClient;
import com.amazonaws.services.rds.model.DBInstance;
import com.amazonaws.services.rds.model.DescribeDBInstancesRequest;
import com.amazonaws.services.rds.model.DescribeDBInstancesResult;
import com.amazonaws.services.rds.model.Endpoint;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;

//Step 3
public class DBSchemaCreation implements RequestHandler<Map<String, String>, Object>
{

    @Override
    public Object handleRequest(Map<String, String> input, Context context) {
        AmazonRDS client = AmazonRDSClient.builder().build();

        System.out.println(input);

        DescribeDBInstancesResult dbInstancesResponse =
                client.describeDBInstances(new DescribeDBInstancesRequest().withDBInstanceIdentifier(input.get(
                        "DBInstanceIdentifier")));
        DBInstance dbInstance = dbInstancesResponse.getDBInstances().get(0);
        Endpoint endpoint = dbInstance.getEndpoint();

        String url = "jdbc:mysql://" + endpoint.getAddress() + ":" + endpoint.getPort();
        String masterUsername = input.get("MasterUsername");
        String masterPassword = input.get("MasterUserPassword");

        try (Connection conn = DriverManager.getConnection(url, masterUsername, masterPassword);
             Statement statement = conn.createStatement())
        {
            statement.execute("CREATE DATABASE testAWS");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return input;
    }
}