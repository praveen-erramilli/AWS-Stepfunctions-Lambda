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

//Step 2
public class DBUserCreation implements RequestHandler<Map<String, Object>, Object>
{

    public Object handleRequest(Map<String, Object> input, Context context) {

        AmazonRDS client = AmazonRDSClient.builder().build();

        System.out.println(input);

        DescribeDBInstancesResult dbInstancesResponse =
                client.describeDBInstances(new DescribeDBInstancesRequest().withDBInstanceIdentifier((String) input.get(
                        "DBInstanceIdentifier")));
        DBInstance dbInstance = dbInstancesResponse.getDBInstances().get(0);
        Endpoint endpoint = dbInstance.getEndpoint();

        String url = "jdbc:mysql://" + endpoint.getAddress() + ":" + endpoint.getPort();
        String masterUsername = (String) input.get("MasterUsername");
        String masterPassword = (String) input.get("MasterUserPassword");

        System.out.println(url);
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        System.out.println("Creating user");
        try(Connection conn = DriverManager.getConnection(url, masterUsername, masterPassword);
            Statement stmt = conn.createStatement())
        {
            String readOnlyUser = "read_only_user";
            String readOnlyUserPswd = "readOnlyPSWD";

            stmt.execute("CREATE USER '" + readOnlyUser + "'@'%' IDENTIFIED BY '" + readOnlyUserPswd + "' ;");
            stmt.execute("GRANT SELECT, SHOW DATABASES ON *.* TO '" + readOnlyUser + "'@'%' WITH GRANT OPTION;");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        System.out.println("Done creating user");
        return input;
    }
}
