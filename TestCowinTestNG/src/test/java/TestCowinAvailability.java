import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class TestCowinAvailability {
	WebDriver driver;
	Actions action;
	WebDriverWait wait;
	String place;
	List<String> a=new ArrayList<String>();
	@BeforeTest
	public void openBrowser() {
		System.setProperty("webdriver.chrome.driver", "F:\\Selenium\\webdriver\\chromedriver.exe");
		driver= new ChromeDriver();
		action=new Actions(driver);
		driver.manage().window().maximize(); 
		wait=new WebDriverWait(driver,10);
	}
	
	@Test(priority = 1)
	public void openURL() {
		driver.get("https://www.cowin.gov.in/home");
		Assert.assertEquals(driver.getTitle(),"CoWIN", "Page not loaded properly");
	}
	
	@Test(priority = 2)
	public void searchByDistrictOption() {
		WebElement toggleButton=driver.findElement(By.xpath("//*[@data-checked='Search By District']"));
		try {
		if(driver.findElement(By.xpath("//*[@placeholder='Enter your PIN']")).isDisplayed()) {
		toggleButton.click();
		System.out.println("Search by option changed to District");
		}
		}catch(Exception e) {
			System.out.println("Select by District already");
		}
	}
	
	@Test(priority = 3)
	public void searchByDistrict() throws Exception {
		String state="Tamil Nadu";
		String district= "Chennai";
		//Thread.sleep(3000);
		wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath("//*[@formcontrolname='state_id']"))));
		WebElement stateDropdown=driver.findElement(By.xpath("//*[@formcontrolname='state_id']/div/div[2]/div"));
		stateDropdown.click();
		wait.until(ExpectedConditions.elementToBeClickable(driver.findElement(By.xpath("//span[contains(text(),'"+state+"')]"))));
		WebElement keralaState=driver.findElement(By.xpath("//span[contains(text(),'"+state+"')]"));
		keralaState.click();
		System.out.println("State selected as "+state);
		WebElement districtDropdown=driver.findElement(By.xpath("//*[@formcontrolname='district_id']/div/div[2]/div"));
		try {
		districtDropdown.click();
		WebElement selectDistrict=driver.findElement(By.xpath("//span[contains(text(),'"+district+"')]"));
		selectDistrict.click();
		}catch(Exception e) {
			Thread.sleep(1000);
			districtDropdown.click();
			WebElement selectDistrict=driver.findElement(By.xpath("//span[contains(text(),'"+district+"')]"));
			selectDistrict.click();
		}
		System.out.println("District selected as "+district);
		WebElement searchButton=driver.findElement(By.xpath("//button[text()='Search']"));
		searchButton.click();
		System.out.println("Search Button clicked");
		wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("(((//span[text()='Previous']//ancestor::div[4])/following-sibling::div//div)[1]//div)[2]/div[4]")));
		WebElement hospitalList=driver.findElement(By.xpath("(((//span[text()='Previous']//ancestor::div[4])/following-sibling::div//div)[1]//div)[2]/div[4]"));
		action.moveToElement(hospitalList).build().perform();
		System.out.println("Search result displayed");
	}
	
	@Test(priority = 4)
	public void searchForLocations() throws IOException {
		readFromExcel();
		XSSFWorkbook workbook=new XSSFWorkbook();
		for(int n=0;n<a.size();n++) {
	    	place=a.get(n);
		System.out.println("Place name entered: "+place+":-");
		place=place.toUpperCase();
		XSSFSheet sheet= workbook.createSheet(place);
		int r=0;
		int x=1;//file name variable
		List<WebElement> centerAddress= driver.findElements(By.xpath("(//div[@class='center-box'])//p[@class='center-name-text']"));
		List<WebElement> centerName= driver.findElements(By.xpath("//h5[@class='center-name-title']"));
		int sizeofList=centerAddress.size();
		for(int i=0;i<sizeofList;i++) {
			if(centerAddress.get(i).getText().toUpperCase().contains(place)) {
				x++;
				Row row= sheet.createRow(r++);
				Cell cell=row.createCell(0);
				cell.setCellValue("Center Name");
				cell=row.createCell(1);
				cell.setCellValue(centerName.get(i).getText());
				row= sheet.createRow(r++);
				cell=row.createCell(0);
				cell.setCellValue("Center Address");
				cell=row.createCell(1);
				cell.setCellValue(centerAddress.get(i).getText());
				System.out.println("Center Name: "+centerName.get(i).getText());
				System.out.println("Center Address: "+centerAddress.get(i).getText());
				int q=0;
				action.moveToElement(centerAddress.get(i)).build().perform();
				
				takeScreenshot(x);//Takes screenshot
				
				List<WebElement> vaccineAvailability=driver.findElements(By.xpath("((((//div[@class='center-box'])//p[@class='center-name-text'])//ancestor::div[2]/following-sibling::div)["+(i+1)+"])/ul/li"));//list from each date
				
				int k=0;
				for(int j=0;j<(vaccineAvailability.size());j++) {
					List<WebElement> vaccineList=driver.findElements(By.xpath("((((//div[@class='center-box'])//p[@class='center-name-text'])//ancestor::div[2]/following-sibling::div)["+(i+1)+"])/ul/li["+(j+1)+"]/div"));
					int vaccineInaDay=vaccineList.size();
					for(int m=1;m<=vaccineInaDay;m++) {
						WebElement vaccineCount=driver.findElement(By.xpath("(((((//div[@class='center-box'])//p[@class='center-name-text'])//ancestor::div[2]/following-sibling::div)["+(i+1)+"])/ul/li["+(j+1)+"]/div//a)["+m+"]"));
						
						if(!(vaccineCount.getText().equals("NA"))) {
							if(!(vaccineCount.getText().equals("Booked"))){				
								q++;
								WebElement vaccineType= driver.findElement(By.xpath("(((((//div[@class='center-box'])//p[@class='center-name-text'])//ancestor::div[2]/following-sibling::div)["+(i+1)+"])/ul/li["+(j+1)+"]/div//a)["+m+"]/following-sibling::div[1]/h5"));
								WebElement vaccineAgeLimit= driver.findElement(By.xpath("(((((//div[@class='center-box'])//p[@class='center-name-text'])//ancestor::div[2]/following-sibling::div)["+(i+1)+"])/ul/li["+(j+1)+"]/div//a)["+m+"]/following-sibling::div[3]/span"));
								List<WebElement> vaccineDose=driver.findElements(By.xpath("(((((//div[@class='center-box'])//p[@class='center-name-text'])//ancestor::div[2]/following-sibling::div)["+(i+1)+"])/ul/li["+(j+1)+"]/div//a)["+m+"]/following-sibling::div[2]/span"));
								WebElement availableDate=driver.findElement(By.xpath("(//li[@class='availability-date'])["+(j+1)+"]//p"));
								row= sheet.createRow(r++);
								cell=row.createCell(0);
								cell.setCellValue("Available Date");
								cell=row.createCell(1);
								cell.setCellValue(availableDate.getText());
								System.out.println("Available Date: "+availableDate.getText());
								row= sheet.createRow(r++);
								cell=row.createCell(0);
								cell.setCellValue("Vaacine Type");
								cell=row.createCell(1);
								cell.setCellValue(vaccineType.getText());
								System.out.println("Vaacine Type: "+vaccineType.getText());
								row= sheet.createRow(r++);
								cell=row.createCell(0);
								cell.setCellValue("Age Limit");
								cell=row.createCell(1);
								cell.setCellValue(vaccineAgeLimit.getText());
								System.out.println("Age Limit: "+vaccineAgeLimit.getText());
								System.out.println("Availability:");
								for(WebElement doseElement:vaccineDose) {
									String[] dose=doseElement.getText().split(" ",2);
									System.out.println(dose[0]+" "+dose[1]);
									row= sheet.createRow(r++);
									cell=row.createCell(0);
									cell.setCellValue(dose[0]);
									cell=row.createCell(1);
									cell.setCellValue(dose[1]);
								}		
								row= sheet.createRow(r++);
								k++;
							}
						}
					}
				}
				if(q==0) {
					row= sheet.createRow(r++);
					cell=row.createCell(0);
					cell.setCellValue("No slots available for this center");
					row= sheet.createRow(r++);
					row= sheet.createRow(r++);
				}
				
				if(k==0) {
					System.out.println("Vaccine not available for any dates");
				}
				System.out.println("\n");
			}
		}
		if(x==1) {
			System.out.println("No matches found for "+place+"\n");
				Row row=sheet.createRow(r++);
				Cell cell=row.createCell(0);
				cell.setCellValue("No slots available for this location");
		}	
	}
		FileOutputStream fop=new FileOutputStream("F:\\Eclipse\\My Workspace\\TestCowinTestNG\\Output\\vaccine.xlsx");
		workbook.write(fop);
	}
	
	@AfterTest
	public void closeBrowser() {
		driver.quit();
	}
	
	public void readFromExcel() throws IOException {
		int w=0;
		File file =    new File("F:\\Eclipse\\My Workspace\\TestCowinTestNG\\Input\\Keywords.xlsx");
	
		try {
			FileInputStream inputStream = new FileInputStream(file);
			XSSFWorkbook keywordWorkbook = new XSSFWorkbook(inputStream);
			XSSFSheet keywordSheet= keywordWorkbook.getSheet("Keys");
			int rowCount = keywordSheet.getLastRowNum()-keywordSheet.getFirstRowNum();
		    for (int i = 0; i <= rowCount; i++) {
		        Row row = keywordSheet.getRow(i);
		        for (int j = 0; j < row.getLastCellNum(); j++) {
		        	a.add(w++, row.getCell(j).getStringCellValue());
		        }
		    } 	    
		    
		    keywordWorkbook.close();
		} catch (FileNotFoundException e) {
			System.out.println("Input file not found");
			e.printStackTrace();
		}	    
	}
	
	public void takeScreenshot(int x) throws IOException {
    	File srcFile=((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		File destFile=new File("F:\\Eclipse\\My Workspace\\TestCowinTestNG\\Screenshots\\"+x+".png");
		FileUtils.copyFile(srcFile, destFile);
    }
	
}
