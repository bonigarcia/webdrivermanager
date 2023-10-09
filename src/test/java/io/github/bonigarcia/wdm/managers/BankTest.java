package io.github.bonigarcia.wdm.managers;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import io.github.bonigarcia.wdm.WebDriverManager;

import org.junit.*;

class App {
    private WebDriver driver;
    String buttons[];
    float retiro = 10;
    float deposite = 10;

    
    public App(){
        WebDriverManager.chromedriver().setup();
        this.driver = new ChromeDriver();

        // Abre la página web
        this.driver.get("https://www.globalsqa.com/angularJs-protractor/BankingProject/#/login");
    }

    @Test
    public void Ejecutar(){
        buttons = new String[4];

        buttons[0] = "//button[contains(@class, 'btn btn-primary btn-lg')]";
        buttons[1] = "//button[contains(@class, 'btn btn-default')]";
        buttons[2] = "//button[contains(@class, 'btn btn-lg tab')]";
        buttons[3] = "//button[contains(@class, 'btn btn-default')]";
        // darle tiempo de espera a la pagina

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        // Encuentra el elemento del botón
        ClickBtn(buttons[0], false, 0);
        List<WebElement> buttonSelect = driver
                .findElements(By.xpath("//option[contains(@class, 'ng-binding ng-scope')]"));
        buttonSelect.get(1).click();

        ClickBtn(buttons[1], false, 0);

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

        ClickBtn(buttons[2], true, 1);

        WebElement inputAmout = driver.findElement(By.cssSelector("input[ng-model='amount']"));
        inputAmout.sendKeys(String.valueOf(retiro));

        ClickBtn(buttons[3], false, 0);
        WebElement spansuccess = this.driver.findElement(By.cssSelector("span[ng-show='message']"));

        System.out.println(spansuccess.isDisplayed());
    }

    public void ClickBtn(String xpath, boolean list, int index){
        if(!list){
            WebElement button = driver.findElement(By.xpath(xpath));
            button.click();
        }
        else{
            List<WebElement> buttonSelect = driver
                .findElements(By.xpath(xpath));
            buttonSelect.get(index).click();
        }
    }

    @Test 
    public void ExisteDeposito(){
        Ejecutar();
        WebElement spansuccess = this.driver.findElement(By.cssSelector("span[ng-show='message']"));;
        Assert.assertTrue(spansuccess.isDisplayed());
        Retirar();
    }

    public void Retirar(){
        WebElement withdraw = driver.findElement(By.cssSelector("button[ng-class='btnClass3']"));
        withdraw.click();
        WebElement inputAmout = driver.findElement(By.xpath("//input[contains(@class, 'form-control ng-pristine ng-untouched ng-invalid ng-invalid-required')]"));
        inputAmout.sendKeys(String.valueOf(retiro));

        WebElement buttonretirar = driver.findElement(By.xpath("//button[contains(@class, 'btn btn-default')]"));
        buttonretirar.click();
    }

    @Test
    public void HayRetiro(){
        ExisteDeposito();
        WebElement retirosuccess = this.driver.findElement(By.xpath("//span[contains(@class, 'error ng-binding')]"));;
        Assert.assertTrue(retirosuccess.isDisplayed());
    }

    @Test
    public void RestoBalance(){
        ExisteDeposito();
        List<WebElement> balance = this.driver.findElements(By.xpath("//strong[contains(@class, 'ng-binding')]"));
        float resultado = deposite - retiro;
        Assert.assertEquals(String.valueOf(Math.round(resultado)), balance.get(1).getText());
    }

    @AfterClass
    public static void tearDown(){
       new App().driver.quit();
    }

    public static void main(String[] args) throws Exception {
        App app = new App();
        app.RestoBalance();
    }
}