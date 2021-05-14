package com.covid.warriors.service.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.covid.warriors.request.model.Account;
import com.covid.warriors.request.model.Message;
import com.covid.warriors.request.model.MessageRequest;
import com.covid.warriors.service.CovidWarriorsSmsService;

import okhttp3.Headers;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class CovidWarriorSmsServiceImpl implements CovidWarriorsSmsService {

	@Value("${api.url}")
	private String apiUrl;

	@Value("${instance.id}")
	private String instanceId;

	@Value("${instance.id.forward}")
	private String instanceIdForwards;		
	
	@Value("${token.forward}")
	private String tokenForForwards;		
	
	@Value("${token}")
	private String token;

	@Value("${resend.wait.min}")
	private int resendWaitMin;

	@Value("${stop.message.sent.count}")
	private int stopMessageSentCount;
	
	@Value("${days.before}")
	private int daysBefore;
	
	@Value("${forward.feature}")
	private boolean forwardFeatureOn;
	
	@Value("${message.response}")
	private String responseMessage;

	@Value("${subscription.message1}")
	private String subscriptionMessage1; 
	
	@Value("${subscription.message2}")
	private String subscriptionMessage2; 
	
	@Value("${forward.before}")
	private int forwardBefore;
	
	@Value("${forward.message.sms.limit}")
	private int forwardMsgSmsLimit;
	
	@Value("${send.help.message}")
	private String helpMessageText;
	
//	@Value("#{'${valid.response.black.list}'.split(',')}")
	@Value("#{'${valid.response.black.list}'.split(',')}")
	private List<String> blackListOfResponses;

	
	@Value("#{'${valid.response.black.list.forward}'.split(',')}")
	private List<String> blackListOfForwardResponses;
	
	@Value("${send.help.message.sms}")
	private String smsMessage;
	
	@Autowired 
	private UrlService urlService;
	
	@Value("${sms.link}")
	private String smsLink;
	
	@Value("${sms.api.link}")
	private String smsApiEndpoint;
	
	@Value("${sms.api.send}")
	private String smsApiSendContext;
	
	@Value("${sms.api.key}")
	private String apiKey;
	
	@Value("${sms.sender.id}")
	private String senderId;
	
	@Value("${sms.channel}")
	private String channel;
	
	@Value("${sms.dcs}")
	private String dcs;
	
	@Value("${sms.route}")
	private String route;
			
	@Autowired
	private RestTemplate restTemplate;
	
	@Override
	@Async
	public String sendSms(List<String> mobileList, String msg) {
		if (!CollectionUtils.isEmpty(mobileList)) {
			Set<String> distinctNumbers = new LinkedHashSet<String>();  
			mobileList.forEach(contact -> {
				if(distinctNumbers.size() == forwardMsgSmsLimit) {
					try {
						sendBulkSmsGatewayHub(msg, String.join(",",distinctNumbers));
						//sendBulkSmsSmsMarketing(msg, String.join("\n",distinctNumbers));
					} catch (Exception e) {
						System.out.println("Error while sending messages to  ; " + distinctNumbers);
						e.printStackTrace();
					}
					distinctNumbers.clear();
				}
				/*String contactStr = getPhoneNumber(contact);
				if(StringUtils.isNotBlank(contactStr)) {
					distinctNumbers.add(contactStr);
				}*/
				if(StringUtils.isNotBlank(contact)) {
					distinctNumbers.add(contact);
				}
			});		
		/*		asyncCovidWarriorServiceImpl.sendAsyncMessage(contact, customMessage.getCity(), customMessage.getCategory(), message);
			});*/
			
			if(!CollectionUtils.isEmpty(distinctNumbers)) {
				try {
					sendBulkSmsGatewayHub(msg, String.join(",",distinctNumbers));
					//sendBulkSmsSmsMarketing(msg, String.join("\n",distinctNumbers));
				} catch (Exception e) {
					System.out.println("Error while sending messages to  ; " + distinctNumbers);
					e.printStackTrace();
				}
			}
			return "Message Successfully Sent";
		}
		System.out.println("Message Sebt Syccessfully to  : " + mobileList.size());
		return "No Data for given City And Category";
	}
	
	private String getPhoneNumber(String contact) {
		contact = contact.replaceAll("[()\\s-]+", "");
		contact = contact.trim().replaceAll(" ", "");
		if (contact.contains("+")) {
			contact = contact.replace("+", "");
		}
		Pattern p1 = Pattern.compile("(91)?[6-9][0-9]{9}");
		Pattern p2 = Pattern.compile("(0)?[6-9][0-9]{9}");
		Matcher m1 = p1.matcher(contact);
		Matcher m2 = p2.matcher(contact);
		boolean isPhoneWithNineOne = (m1.find() && m1.group().equals(contact));
		boolean isPhoneWithZero = (m2.find() && m2.group().equals(contact));
		if (isPhoneWithNineOne) {
			if(contact.length() == 10) {
				return contact;
			}
			return contact.substring(2, contact.length());
		} else if (isPhoneWithZero) {
			return contact.substring(1, contact.length());
		}
		return "";
	}
	
	public void sendBulkSmsSmsMarketing(String msg, String mobileNumbers) throws IOException {
		OkHttpClient client = new OkHttpClient().newBuilder()
				  .build();
		Request request = new Request.Builder()
				  .url("http://smsmarketing.site/auth/login")
				  .method("GET", null)
				  .addHeader("DNT", "1")
				  .build();
				client = new OkHttpClient().newBuilder()
				  .build();
		Response response = client.newCall(request).execute();
		if(response.isSuccessful()) {
			response.close();
			RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
			  .addFormDataPart("csrf_test_name","5974ccf92f3c86662606e375f9ab9665")
			  .addFormDataPart("username","rajimahajan")
			  .addFormDataPart("password","134679")
			  .addFormDataPart("submit","Login")
			  .build();
			request = new Request.Builder()
			  .url("http://smsmarketing.site/auth/login")
			  .method("POST", body)
			  .addHeader("DNT", "1")
			  .addHeader("Cookie", "csrf_cookie_name=5974ccf92f3c86662606e375f9ab9665; ci_session=858d509edad4ba38853d8fc0aafda769a9ecd26d;")
			  .build();
			response = client.newCall(request).execute();
			if(response.isSuccessful()) {
				response.close();
				Headers headers = response.headers();
				System.out.println("test : "  + headers);
				body = new MultipartBody.Builder().setType(MultipartBody.FORM)
				  .addFormDataPart("csrf_test_name","5974ccf92f3c86662606e375f9ab9665")
				  .addFormDataPart("Compose_smsForm[unicode]","0")
				  .addFormDataPart("Compose_smsForm[unicode]","0")
				  .addFormDataPart("Compose_smsForm[flash]","0")
				  .addFormDataPart("language","am")
				  .addFormDataPart("Compose_smsForm[message]",msg)
				  .addFormDataPart("counting",msg.length()+"")
				  .addFormDataPart("Compose_smsForm[send_to]","1")
				  .addFormDataPart("Compose_smsForm[mobile]",mobileNumbers)
				  .addFormDataPart("Compose_smsForm[remove_duplicate]","0")
				  .addFormDataPart("Compose_smsForm[remove_duplicate]","1")
				  .addFormDataPart("Compose_smsForm[schedule_sms]","0")
				  .addFormDataPart("Compose_smsForm[schedule_date]","")
				  .addFormDataPart("hour","")
				  .addFormDataPart("minute","")
				  .addFormDataPart("second","")
				  .addFormDataPart("Compose_smsForm[schedule_time]",getTimeForSendingMessages())
				  .addFormDataPart("submit-button","")
				  .build();
				request = new Request.Builder()
				  .url("http://smsmarketing.site/SendSms/GSMcompose")
				  .method("POST", body)
				  .addHeader("Cookie", "csrf_cookie_name=5974ccf92f3c86662606e375f9ab9665; ci_session=858d509edad4ba38853d8fc0aafda769a9ecd26d; csrf_cookie_name=5974ccf92f3c86662606e375f9ab9665; ci_session=858d509edad4ba38853d8fc0aafda769a9ecd26d")
				  .addHeader("DNT", "1")
				  .build();
				response = client.newCall(request).execute();
			
				request = new Request.Builder()
						  .url("http://smsmarketing.site/auth/logout")
						  .method("GET", null)
						  .addHeader("DNT", "1")
						  .addHeader("Cookie", "csrf_cookie_name=5974ccf92f3c86662606e375f9ab9665; ci_session=0c048cf3fa6c1a0b9fcd7fc13fbf18810b047bf5; csrf_cookie_name=5974ccf92f3c86662606e375f9ab9665")
						  .build();
				response = client.newCall(request).execute();
				
				response.close();
			}
		}
	}
	
	public String getTimeForSendingMessages() {
		DateFormat dateFormat = new SimpleDateFormat("hh:mm aa");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, 5);
		Date date = cal.getTime();
    	return dateFormat.format(date).toString();
    }

	@Override
	public String sendSms(List<String> mobileList, String city, String category, String subCat) {
		if (!CollectionUtils.isEmpty(mobileList)) {
			Set<String> distinctNumbers = new LinkedHashSet<String>();  
			mobileList.forEach(contact -> {
				//String contantStr = getPhoneNumber(contact);
				String msg = prepareSmsMessage(city, category, contact, subCat);
				System.out.println("message length : " + msg.length());
				try{
					sendBulkSmsGatewayHub(msg, contact);
				//	sendBulkSmsSmsMarketing(msg, contantStr);
				} catch (Exception e) {
					System.out.println("Error while sending messages to  ; " + distinctNumbers);
					e.printStackTrace();
				}				
			});		
			System.out.println("Message Sent Syccessfully to  : " + mobileList.size());
			return "Message Successfully Sent";
		}
		System.out.println("Message Sebt Syccessfully to  : " + mobileList.size());
		return "No Data for given City And Category";
	}
	
	private String prepareSmsMessage(String city, String category, String contact, String subCat) {
		String messageStr = smsMessage;
		String requestParamYes = "a=y&m=" + contact + "&c=" + category + "&ct=" + city;
		String requestParamNo = "a=n&m=" + contact + "&c=" + category + "&ct=" + city;

		String paramYes = urlService.convertToShortUrl(requestParamYes);
		String smsLinkStrYes = smsLink;
		smsLinkStrYes = smsLinkStrYes + "p=" + paramYes;
		
		String paramNo = urlService.convertToShortUrl(requestParamNo);
		String smsLinkStrNo= smsLink;
		smsLinkStrNo = smsLinkStrNo + "p=" + paramNo;
		
		return messageStr.replace("!cat!", subCat).replace("!yeslink!", smsLinkStrYes).replace("!nolink!", smsLinkStrNo);
	}
	
	
	public void sendBulkSmsGatewayHub(String msg, String mobileNumbers) {
		
		Account account = new Account();
		account.setAPIKey(apiKey);
		account.setSenderId(senderId);
		account.setChannel(channel);
		account.setDCS(dcs);
		account.setRoute(route);
		
		Message message = new Message();
		List<Message> listOfMessages = new ArrayList<>();
		
		message.setNumber(mobileNumbers);
		message.setText(msg);
		
		listOfMessages.add(message);
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		Map<String, Object> request = new HashMap<>();
		request.put("Account", account);
		request.put("Messages", listOfMessages);
		
		HttpEntity<?> entity = new HttpEntity<>(request, headers);
		String response = restTemplate.exchange(smsApiEndpoint + smsApiSendContext, HttpMethod.POST, entity, String.class).getBody();
		System.out.println(response);
	}
}
