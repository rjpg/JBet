package marketNavigator;

import generated.exchange.BFExchangeServiceStub.Market;
import generated.global.BFGlobalServiceStub.BFEvent;
import generated.global.BFGlobalServiceStub.EventType;
import generated.global.BFGlobalServiceStub.GetEventsResp;
import generated.global.BFGlobalServiceStub.MarketSummary;

import java.awt.BorderLayout;
import java.util.Vector;

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
import javax.swing.tree.TreePath;

import main.Parameters;
import DataRepository.MarketData;
import DataRepository.MarketProvider;
import DataRepository.MarketProviderListerner;
import GUI.MarketMainFrame;

import demo.handler.ExchangeAPI;
import demo.handler.GlobalAPI;
import demo.handler.ExchangeAPI.Exchange;
import demo.util.APIContext;

public class MarketNavigator extends MarketProvider {
	
	private APIContext apiContext=null;
	private Exchange selectedExchange=null;
	private JTree tree=null;
	
	private JPanel panel =null;
		
	private Market selectedMarket=null;
	
	private Vector<MarketProviderListerner> listeners=new Vector<MarketProviderListerner>();
		
	public JPanel getPanel() {
		return panel;
	}


	public MarketNavigator(APIContext apiC, Exchange selectedExchangeA) {
		apiContext=apiC;
		selectedExchange=selectedExchangeA;
		initialize();
	}
	
	
	private void initialize()
	{
		panel=new JPanel();
		
		panel.setLayout(new BorderLayout());
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
					
					if(selectedMarket!=null && m.getMarketId()==selectedMarket.getMarketId())
						return;
					selectedMarket=m;
					
					warnListeners();
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
		
		tree.expandPath(new TreePath(dmtn.getPath()));
		panel.add(tree,BorderLayout.CENTER);
		
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

	
	@Override
	public void addMarketProviderListener(MarketProviderListerner mpl) {
		listeners.add(mpl);
		
	}


	@Override
	public void removeMarketProviderListener(MarketProviderListerner mpl) {
		listeners.remove(mpl);
		
	}

	private void warnListeners()
	{
		if(selectedMarket==null) return;
		
		for(MarketProviderListerner mpl:listeners)
			mpl.newMarketSelected(this, selectedMarket);
	}

	@Override
	public Market getCurrentSelectedMarket() {
		return selectedMarket;
	}
	
}
