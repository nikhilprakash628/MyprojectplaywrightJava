package caseKroPlayWright;
import com.microsoft.playwright.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class Product {
    String title;
    double discountedPrice;
    double actualPrice;
    String imageUrl;

    public Product(String title, double discountedPrice, double actualPrice, String imageUrl) {
        this.title = title;
        this.discountedPrice = discountedPrice;
        this.actualPrice = actualPrice;
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Title: " + title + "\nDiscounted Price: " + discountedPrice +
                "\nActual Price: " + actualPrice + "\nImage URL: " + imageUrl + "\n";
    }
}

public class CaseKaro {
    public static void main(String[] args) {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setChannel("msedge")
                .setHeadless(false));

        Page page = browser.newPage();
        System.out.println("Opening CaseKaro website...");
        page.navigate("https://casekaro.com/");
        page.waitForLoadState();
        page.waitForTimeout(3000);
        System.out.println("Clicking on 'Mobile Covers'...");
        page.locator("#HeaderMenu-mobile-covers").click();
        page.waitForLoadState();
        page.waitForTimeout(3000);
        System.out.println("Searching for 'Apple'...");
        page.locator("input[type='search']").fill("Apple");
        page.locator("button[type='submit']").click();
        page.waitForLoadState();
        page.waitForTimeout(3000);
        System.out.println("Validating search results...");
        List<String> productTitles = page.locator(".product-title").allTextContents();
        for (String title : productTitles) {
            if (!title.contains("Apple")) {
                throw new AssertionError("Validation Failed: Non-Apple product found - " + title);
            }
        }
        System.out.println("Validation Passed: All products are for Apple!");
        System.out.println("Searching for 'iPhone 16 Pro'...");
        page.locator("text=Apple").click();
        page.locator("input[type='search']").fill("iPhone 16 Pro");
        page.locator("button[type='submit']").click();
        page.waitForLoadState();
        page.waitForTimeout(3000);
        System.out.println("Applying 'In Stock' filter...");
        page.locator("label:has-text('In Stock')").click();
        page.waitForLoadState();
        page.waitForTimeout(3000);
        List<Product> productList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            System.out.println("Fetching product details from page " + (i + 1));
            List<ElementHandle> items = page.locator(".product-item").elementHandles();
            for (ElementHandle item : items) {
                String title = item.querySelector(".product-title").innerText();
                double discountedPrice = Double.parseDouble(item.querySelector(".discounted-price").innerText().replace("₹", "").trim());
                double actualPrice = Double.parseDouble(item.querySelector(".actual-price").innerText().replace("₹", "").trim());
                String imageUrl = item.querySelector(".product-image img").getAttribute("src");

                productList.add(new Product(title, discountedPrice, actualPrice, imageUrl));
            }
            if (page.locator("text=Next").isVisible()) {
                page.locator("text=Next").click();
                page.waitForLoadState();
                page.waitForTimeout(3000);
            } else {
                break;
            }
        }
        System.out.println("Sorting products by discounted price...");
        Collections.sort(productList, Comparator.comparingDouble(p -> p.discountedPrice));

        System.out.println("\n=== Sorted Product List ===");
        for (Product product : productList) {
            System.out.println(product);
        }
        browser.close();
        playwright.close();
    }
}
