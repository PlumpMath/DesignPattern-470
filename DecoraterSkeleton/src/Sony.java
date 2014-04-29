
public class Sony extends CondimentDecorator{
    
	Beverage beverage;
	
	public Sony(Beverage beverage){
		this.beverage = beverage;
	}
	
	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return beverage.getDescription()+ ", Sony";
	}

	@Override
	public double cost() {
		// TODO Auto-generated method stub
		return .30+beverage.cost();
	}

}
