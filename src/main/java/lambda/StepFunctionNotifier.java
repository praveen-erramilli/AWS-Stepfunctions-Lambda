package lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.stepfunctions.AWSStepFunctions;
import com.amazonaws.services.stepfunctions.AWSStepFunctionsClientBuilder;
import com.amazonaws.services.stepfunctions.model.SendTaskSuccessRequest;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */

//Step 1.1
public class StepFunctionNotifier implements RequestHandler<Map<String, Object>, Object> {

    public Object handleRequest(final Map<String, Object> input, final Context context) {
        System.out.println(input);
        Map<String, Object> resourceProperties = (Map<String, Object>) input.get("ResourceProperties");
        SendTaskSuccessRequest successRequest = new SendTaskSuccessRequest();
        successRequest.setTaskToken((String)resourceProperties.get("Token"));

        String outputData = (String) resourceProperties.get("Output");
        successRequest.setOutput(outputData);

        System.out.println("Notifying stepfunction");
        AWSStepFunctions client = AWSStepFunctionsClientBuilder.defaultClient();
        client.sendTaskSuccess(successRequest);

        System.out.println("Notifying CFM");
        sendResponseToCloudFormation(input, context);
        return null;
    }

    private void sendResponseToCloudFormation(Map<String, Object> input, Context context) {
        String responseUrl = (String) input.get("ResponseURL");
        context.getLogger().log("ResponseURL: " + responseUrl);
        try {
            URL s3URL = new URL(responseUrl);
            HttpURLConnection connection = (HttpURLConnection) s3URL.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("PUT");

            JSONObject responseBody = new JSONObject();
            responseBody.put("Status", "SUCCESS");//or send FAILED incase of failed task
            responseBody.put("PhysicalResourceId", context.getLogStreamName());
            responseBody.put("StackId", input.get("StackId"));
            responseBody.put("RequestId", input.get("RequestId"));
            responseBody.put("LogicalResourceId", input.get("LogicalResourceId"));
//            responseBody.put("Data", "responseData");

            OutputStreamWriter response = new OutputStreamWriter(connection.getOutputStream());
            response.write(responseBody.toString());
            response.close();
            context.getLogger().log("Response Code: " + connection.getResponseCode());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
