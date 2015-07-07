import java.util.Scanner;
import java.io.File;
class TaxEstimator{
	public static void main(String[] args) throws Exception{
		Scanner input = new Scanner(System.in);
		System.out.println("Hello, I will help calculate your max tax, given" +
		"your gross(total) income.\nPlease enter the tax year (e.g 2014):");
		String year = input.next();
		System.out.println("Please type in your gross income for " + year + ":");
		double grossPay = input.nextDouble();
		String[] statuses = {"Single", "Married Filing Jointly or Surviving Spouse", "Head of Household", "Married Filing Separately"}; 
		System.out.println("Finally, where:\n1 => " + statuses[0] + "\n2 => " + 
		statuses[1] + "\n3 => " + statuses[2] + "\n4 => " + statuses[3] + "\n" +
		"Type in the number that represents your filing status for " + year + ":");
		int statusInt = input.nextInt();
		input.close();
		
		String status;
		if(statusInt == 1){
			status = statuses[0];
		} else if(statusInt == 2) {
			status = statuses[1];
		} else if(statusInt == 3) {
			status = statuses[2];
		} else {
			status = statuses[3];
		}

		double[] rates = getRates(year, status);
		if(rates.length < 2){
			System.out.println(year + " tax rates not found. Check back soon.");
			System.exit(0);
		}

		double estimatedTax = calculateTax(rates, grossPay);

		System.out.printf("Your potential tax payment (excluding deductions) is $%5.2f" +
		"\nand your effective tax rate is %2.2f", estimatedTax, ((estimatedTax * 100)/grossPay));
	}

	public static double[] getRates(String year, String status) throws Exception{
		year = "year " + year; //perhaps I should use regex for this
		File file = new File("taxrate.txt");
		Scanner input = new Scanner(file);
		String fileYear, fileStatus;
		
		do {
			fileYear = input.nextLine();
			if(!input.hasNext()) break;
		} while (!fileYear.equals(year));

		if(fileYear.equals(year)){
			do {
				fileStatus = input.nextLine();
			} while(!status.equalsIgnoreCase(fileStatus));
		} else {
			return new double[1];
		}

		double[] rates = new double[14];
		for(int i = 0; i < 14; i++){
			rates[i] = input.nextDouble();
		}
			input.close();
			return rates;
	}

	public static double calculateTax(double[] rates, double grossPay){
		double result = 0.0;

		if(grossPay <= rates[0]){
				return grossPay * rates[1];
		}

		for(int i = rates.length - 2; i >= 0; i -= 2){
			if(grossPay >= rates[i]){
				if(i == rates.length - 2){
					result += (grossPay + 0.01 - rates[i]) * rates[i + 1];
				} else {
					result += i < 2 ? rates[i] * rates[i + 1] : (rates[i] - rates[i -2]) * rates[i+1];
				}
			} else if (grossPay > rates[i - 2]) {
				result += (grossPay - rates[i - 2]) * rates[i + 1];
			}
		}
		return result;
	}
}
