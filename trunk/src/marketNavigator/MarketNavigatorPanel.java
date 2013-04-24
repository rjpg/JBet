package marketNavigator;

import generated.exchange.BFExchangeServiceStub.Market;
import generated.global.BFGlobalServiceStub.BFEvent;
import generated.global.BFGlobalServiceStub.EventType;
import generated.global.BFGlobalServiceStub.GetEventsResp;
import generated.global.BFGlobalServiceStub.MarketSummary;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import main.Parameters;
import DataRepository.MarketData;
import GUI.MarketMainFrame;

import demo.handler.ExchangeAPI;
import demo.handler.GlobalAPI;
import demo.handler.ExchangeAPI.Exchange;
import demo.util.APIContext;

public class MarketNavigatorPanel extends JPanel {
	
	private APIContext apiContext=null;
	private Exchange selectedExchange=null;
	private JTree tree=null;
	
	public MarketNavigatorPanel(APIContext apiC, Exchange selectedExchangeA) {
		apiContext=apiC;
		selectedExchange=selectedExchangeA;
		initialize();
	}
	
	
	private void initialize()
	{
		
		
		this.setLayout(new BorderLayout());
		DefaultMutableTreeNode dmtn=new DefaultMutableTreeNode("Event Types");
		
		tree=new JTree(dmtn);
		
		tree.addTreeExpansionListener(new TreeExpansionListener() {
			
			@Override
			public void treeExpanded(TreeExpansionEvent arg0) {
				DefaultMutableTreeNode node=(DefaultMutableTreeNode) arg0.getPath().getPathComponent(arg0.getPath().getPathCount()-1);
				
				System.out.println(node.getUserObject());
				
				if(node.getUserObject() instanceof EventTypeObj)
				{
					GetEventsResp respin=null;
					try {
						respin=GlobalAPI.getEvents(apiContext, ((EventTypeObj)node.getUserObject()).eventType.getId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(respin== null)
						return ;
					
					node.removeAllChildren();
					
					
					fillTree(respin, node);
					tree.repaint();
					
					return;
				}
				
				if(node.getUserObject() instanceof EventObj)
				{
					System.out.println("passei aqui ");
					GetEventsResp respin=null;
					try {
						respin=GlobalAPI.getEvents(apiContext, ((EventObj)node.getUserObject()).event.getEventId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(respin== null)
						return ;
					
					node.removeAllChildren();
					
					
					fillTree(respin, node);
					tree.repaint();
					
					return;
				}
				
				
   
				
			}
			
			@Override
			public void treeCollapsed(TreeExpansionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			
			@Override
			public void valueChanged(TreeSelectionEvent arg) {
				DefaultMutableTreeNode node=(DefaultMutableTreeNode) arg.getPath().getPathComponent(arg.getPath().getPathCount()-1);
				
				if(node.getUserObject() instanceof MarketObj)
				{
					System.out.println("passei aqui é market");
					Market m=null;
					try {
						m = ExchangeAPI.getMarket(selectedExchange, apiContext, ((MarketObj)node.getUserObject()).market.getMarketId());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(m==null)
						return;
					
					MarketData md = new MarketData(m, selectedExchange,apiContext);
					if(Parameters.graphicalInterface)
					{
						//System.out.println("passei aqui");
						MarketMainFrame mmf = new MarketMainFrame(md);
						mmf.setSize(400, 600);
						mmf.setVisible(true);
					}
					
					md.startPolling();
					return;
				}
			}
		});
		
		
		EventType et[]=null;
		try {
			et=GlobalAPI.getActiveEventTypes(apiContext);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(et!=null)
		{
			for(EventType evettype:et)
			{
				GetEventsResp respin=null;
				try {
					respin=GlobalAPI.getEvents(apiContext, evettype.getId());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				DefaultMutableTreeNode node=new DefaultMutableTreeNode(new EventTypeObj(evettype));
				node.setAllowsChildren(true);
				node.add(new DefaultMutableTreeNode());
				dmtn.add(node);
				//fillTree(respin, node);
			}
		}
		
		this.add(tree,BorderLayout.CENTER);
	}
	
	private void fillTree(GetEventsResp resp,DefaultMutableTreeNode dmtn)
	{
		if(resp==null)
			return;
		
		BFEvent eventsArray[]=resp.getEventItems().getBFEvent();
		
		if(eventsArray!=null)
		{
			for(BFEvent bfe:eventsArray)
			{
				
				DefaultMutableTreeNode d=new DefaultMutableTreeNode(new EventObj(bfe));
				
				if(!bfe.getEventName().equals("Coupons"))
				{
				d.add(new DefaultMutableTreeNode());
				
				System.out.println(d);
				
				DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
				
				model.insertNodeInto(d, dmtn, dmtn.getChildCount());
							
				model.reload(dmtn);
				
				}
			}
		}
		
		MarketSummary markets[]=resp.getMarketItems().getMarketSummary();
		if(markets!=null)
		{
			
			for(MarketSummary m:markets)
			{
				DefaultMutableTreeNode d=new DefaultMutableTreeNode(new MarketObj(m));
				DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
				
				model.insertNodeInto(d, dmtn, dmtn.getChildCount());
							
				model.reload(dmtn);
			}
			
		}
				
		
	}
	
	public static void main(String[] args)  throws Exception {
		
	}
	
}
