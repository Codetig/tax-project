import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
public class TaxEstimator{
	public static void main(String[] args) throws Exception {
		System.out.println("Hello, I will help calculate your max tax, given " +
		"your gross(total) income.");

		estimatorController();
	
		System.out.println("\nThank you for using Tax Estimator.");
	}

	final static String[] STATUSES = {"Single", "Married Filing Jointly or Surviving Spouse", "Head of Household", "Married Filing Separately"}; 

	public static void estimatorController() throws Exception {
		Scanner input = new Scanner(System.in);
		System.out.println("Please enter the tax year (e.g 2014):");
		
		String strYear = input.next();
		TaxYear year = new TaxYear(strYear);
		if(year.toString().isEmpty()){
			System.out.println(strYear + " tax rates not found. Check back soon. Try another year?");
				return;
			}

		System.out.println("Please type in your gross income for " + year.value() + ":");
		double grossPay = input.nextDouble();
		
		System.out.println("Finally, where:\n1 => " + STATUSES[0] + "\n2 => " + 
		STATUSES[1] + "\n3 => " + STATUSES[2] + "\n4 => " + STATUSES[3] + "\n" +
		"Type in the number that represents your filing status for " + year.value() + ":");
		int statusInt = input.nextInt();
		if(statusInt > 4 || statusInt < 1) {
			System.out.println("Error: Invalid status number.");
			return;
		}
		
		input.close();

		Tax estimatedTax = new Tax(year, grossPay, (statusInt - 1));

		System.out.printf("Your potential tax payment (excluding deductions) is $%5.2f" +
		"\nand your effective tax rate is %2.2f percent", estimatedTax.getMaxTax(), estimatedTax.getEffectiveRate());
	}
}

class Tax implements Comparable<Tax>{
	private String status;
	private int statusInt;
	private double grossPay, maxTax = 0;
	private TaxYear year;

	//construtor
	public Tax(TaxYear year, double grossPay, int statusInt){
		this.year = year;
		this.grossPay = grossPay;
		this.status = TaxEstimator.STATUSES[statusInt];
		this.statusInt = statusInt;
	}

	//impelement compareTo using maxtax value
	@Override
	public int compareTo(Tax o) {
		if(getMaxTax() > o.getMaxTax()) 
			return 1;
		else if(getMaxTax() < o.getMaxTax())
			return -1;
		else
			return 0;
	}

	//overrriding toString method
	public String toString(){
		return "Tax Year: " + year.value() + "\nStatus: " +
		status + "\nGross Pay: " + grossPay;
	}

	//attribute reader methods
	public TaxYear getTaxYear(){
		return year;
	}
	public double getGrossPay(){
		return grossPay;
	}
	public String getStatus(){
		return status;
	}

	//tax calculation methods
	public double getMaxTax(){
		if(grossPay <= 0)
			return 0;
		if(maxTax > 0)
			return maxTax;

		double result = 0.0;
		double[] rates;

		if(statusInt == 1)
			rates = year.single();
		else if(statusInt == 2)
			rates = year.marriedJ();
		else if(statusInt == 3)
			rates = year.head();
		else
			rates = year.marriedS();

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
		maxTax = result;
		return result;
	}

	public double getEffectiveRate(){
		return (getMaxTax() * 100) / grossPay;
	}
}

class TaxYear implements Comparable<TaxYear> {
	private String year;
	private boolean yearFound;
	private ArrayList<Double> single, marriedJ, head, marriedS;

	public TaxYear(String year) throws Exception {
		this.year = year;
		yearFound = false;
		single = new ArrayList<>();
		marriedJ = new ArrayList<>();
		head = new ArrayList<>();
		marriedS = new ArrayList<>();
		setYear();
	}

	private void setYear() throws Exception {
		String strYear = "year " + year;
		File file = new File("taxrate.txt");
		Scanner input1 = new Scanner(file);

		while(input1.hasNextLine()){
			if(strYear.equalsIgnoreCase(input1.nextLine())) {
				yearFound = true;
				break;
			}
		}

		if(yearFound) {
			String placeholder = input1.nextLine();
			while (marriedS.size() < 1) { //marriedS is the last category in my file
				if(placeholder.equalsIgnoreCase("Single"))
					fillList(single, input1.nextLine());
				else if(placeholder.equalsIgnoreCase("Married Filing Jointly or Surviving Spouse"))
					fillList(marriedJ, input1.nextLine());
				else if(placeholder.equalsIgnoreCase("Head of Household"))
					fillList(marriedJ, input1.nextLine());
				else if(placeholder.equalsIgnoreCase("Married Filing Separately"))
					fillList(marriedS, input1.nextLine());
				else
					continue;
				placeholder = input1.nextLine();
			}
		}
		input1.close();
	}

	private void fillList(ArrayList<Double> list, String rates){
			Scanner line = new Scanner(rates);
			while(line.hasNext())
				list.add(line.nextDouble());
			line.close();
	}

	private double[] toArray(ArrayList<Double> list){
		double[] result = new double[list.size()];
		for(int i = 0; i < result.length; i++) {
			result[i] = list.get(i);
		}
		return result;
	}

	//overriding toString
	@Override
	public String toString(){
		return yearFound ? year : "";
	}

	public int value(){
		return Integer.parseInt(year);
	}

	//implementing compareTo
	@Override
	public int compareTo(TaxYear o){
		if(value() > o.value())
			return 1;
		else if(value() < o.value())
			return -1;
		else
			return 0;
	}

	public double[] single() {
		return toArray(single);
	}
	public double[] marriedJ() {
		return toArray(marriedJ);
	}
	public double[] head() {
		return toArray(head);
	}
	public double[] marriedS() {
		return toArray(marriedS);
	}

}
