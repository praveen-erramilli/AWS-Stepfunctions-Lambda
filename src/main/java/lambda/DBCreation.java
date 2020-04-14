package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.CreateStackRequest;
import software.amazon.awssdk.services.cloudformation.model.Parameter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//Step 1
public class DBCreation implements RequestStreamHandler
{
    static final String STACK_NAME = "MySQLCreatorStack";

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        JSONObject jsonObject = new JSONObject(IOUtils.toString(inputStream, StandardCharsets.UTF_8));
        System.out.println("::RequestInput:: " + jsonObject.toString());    //For logging

        JSONObject input = jsonObject.getJSONObject("input");

        JSONObject resourceProperties = new JSONObject();
        resourceProperties.put("MasterUsername", input.optString("DBUser", "admin"));
        resourceProperties.put("MasterUserPassword", input.optString("DBPassword", "praveene"));
        resourceProperties.put("DBInstanceIdentifier", "praveendbinstance");

        List<Parameter> paramsList = getTemplateParameters(input);
        paramsList.add(Parameter.builder().parameterKey("Token").parameterValue(jsonObject.getString("token")).build());
        paramsList.add(Parameter.builder().parameterKey("Output").parameterValue(resourceProperties.toString()).build());

        String template = FileUtils.readFileToString(new File("template.json"), StandardCharsets.UTF_8);
        CreateStackRequest createRequest = CreateStackRequest.builder()
                .stackName(STACK_NAME)
                .templateBody(template)
                .parameters(paramsList)
                .build();

        try (CloudFormationClient client = CloudFormationClient.builder()
                .build())
        {
            client.createStack(createRequest);
        }

        try (Writer w = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))
        {
            w.write(jsonObject.toString());
        }
    }

    private List<Parameter> getTemplateParameters(JSONObject jsonObject) {
        List<Parameter> paramsList = new ArrayList<>();
        String allocatedStorage = jsonObject.optString("DBAllocatedStorage", null);
        if (allocatedStorage != null)
        {
            paramsList.add(Parameter.builder().parameterKey("DBAllocatedStorage").parameterValue(allocatedStorage).build());
        }
        String dbInstanceClass = jsonObject.optString("DBClass", null);
        if (dbInstanceClass != null)
        {
            paramsList.add(Parameter.builder().parameterKey("DBClass").parameterValue(dbInstanceClass).build());
        }
        String dbName = jsonObject.optString("DBName", null);
        if (dbName != null)
        {
            paramsList.add(Parameter.builder().parameterKey("DBName").parameterValue(dbName).build());
        }
        String masterUsername = jsonObject.optString("DBUser", null);
        if (masterUsername != null)
        {
            paramsList.add(Parameter.builder().parameterKey("DBUser").parameterValue(masterUsername).build());
        }
        String masterUserPassword = jsonObject.optString("DBPassword", null);
        if (masterUserPassword != null)
        {
            paramsList.add(Parameter.builder().parameterKey("DBPassword").parameterValue(masterUserPassword).build());
        }
        return paramsList;
    }

}
