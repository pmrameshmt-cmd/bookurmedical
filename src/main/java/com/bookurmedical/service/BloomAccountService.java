package  com.smartstockhub.bloom.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.bson.Document;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bookurmedical.database.BloomAccountDatabase;
import com.bookurmedical.database.BloomSequenceDatabase;

/**
 * This class hanldes account creation on sign up ,fetching the account related
 * data or update an account.
 * 
 * <br>
 *  * 
 * </ul>
 * 
 * <br>
 * 
 * @author RAM (16-06-2025)
 * @since 1.0
 * 
 */

@Service
public class BloomAccountService {

    @Autowired
    private BloomAccountDatabase accountDatabase;

    @Autowired
    private BloomSequenceDatabase sequenceDatabase;

    @Autowired
    private BloomUserService userService;

    public ResponseEntity<JSONObject> accountSignUp(JSONObject requestBody) {
        JSONObject response = new JSONObject();
        try {
            List<String> errorMessages = validateCreateAccount(requestBody);

            if (errorMessages.size() > 0) {
                response.put("errorMessages", errorMessages);
                response.put("status", "error");
                return ResponseEntity.badRequest().body(response);
            }

            Document accountObject = createAccount(requestBody);
            if (accountObject != null && accountObject.containsKey("_id")) {
                Document userObject = userService.createUser(accountObject.getString("shopNumber"), requestBody,
                        "OWNER");
                if (userObject == null || !userObject.containsKey("_id")) {
                    response.put("errorMessages", Arrays.asList("Error in creating user"));
                    response.put("status", "error");
                    return ResponseEntity.badRequest().body(response);
                }
            }

            response.put("status", "success");
            response.put("message", "Account created");
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            response = new JSONObject().put("status", "error");
            response.put("errorMessage", "Internal Server Error");
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(response);
        }

    }

    /**
     * Validation of required fields and email already exists validation are
     * performed here.
     * 
     * @param requestBody : JSONObject with name,email and password
     * @return List of error messages.
     */
    public List<String> validateCreateAccount(JSONObject requestBody) {
        List<String> errorMessages = new ArrayList<>();
        if (requestBody.optString("name", "").isEmpty()) {
            errorMessages.add("Name is invalid");
        }
        if (requestBody.optString("email", "").isEmpty()) {
            errorMessages.add("Email is invalid");
        }
        if (requestBody.optString("password", "").isEmpty()) {
            errorMessages.add("Password is invalid");
        }

        if (errorMessages.size() > 0) {
            return errorMessages;
        }

        // emailid and password format validation check for unicocdes and invalid
        // characters as well

        // requestBody.put("email", requestBody.getString("email").toLowerCase());

        Document searchQuery = new Document();
        Document projection = new Document();
        searchQuery.put("email", Pattern.compile(requestBody.getString("email"), Pattern.CASE_INSENSITIVE));
        Document user = accountDatabase.getSingleUser(searchQuery, projection);

        if (user != null && !user.isEmpty()) {
            errorMessages.add("Email already exists");
            return errorMessages;
        }

        return errorMessages;
    }

    /**
     * Basic account details, are created and inserted in accounts collection.
     * 
     * @param reqJsonObject : JSONObject which contains name,email and password.
     * @return Returns insert account document if inserted successfull or else null
     *         or empty document.
     */
    public Document createAccount(JSONObject reqJsonObject) {

        try {
            String shopNumber = sequenceDatabase.shopNumberSequence();

            Document accountObject = new Document();
            accountObject.put("shopNumber", shopNumber);
            accountObject.put("accountType", "NEW");
            accountObject.put("shopName", reqJsonObject.getString("shopName"));
            accountObject.put("createdAt", System.currentTimeMillis() / 1000L);
            accountObject.put("updatedAt", System.currentTimeMillis() / 1000L);

            accountDatabase.createAccount(accountObject);
            return accountObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Document getAccountByshopNumber(String shopNumber) {

        Document searchQuery = new Document();
        searchQuery.put("shopNumber", shopNumber);

        Document projection = new Document();

        return accountDatabase.getSingleAccount(searchQuery, projection);
    }

    public ResponseEntity<JSONObject> getAccountInfo(String shopNumber, String userEmail) {
        try {
            Document account = getAccountByshopNumber(shopNumber);

            Document user = userService.getSingleUser(userEmail, shopNumber);

            JSONObject response = new JSONObject();
            response.put("account", account);
            response.put("user", user);
            return ResponseEntity.ok().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(new JSONObject().put("errorMessage", "Internal server error"));
        }
    }
    
    

}
