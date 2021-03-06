package gq.vaccum121.ui.kitchen;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Table;
import gq.vaccum121.data.Order;
import gq.vaccum121.data.OrderRepository;
import gq.vaccum121.ui.event.EventSystem;
import gq.vaccum121.ui.event.ReloadEntriesEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Created by vaccum121 on 28.05.16.
 */
@SpringView(name = KitchenUIView.VIEW_NAME)
@UIScope
public class KitchenUIView extends HorizontalLayout implements View, ReloadEntriesEvent.ReloadEntriesListener {
    public static final String VIEW_NAME = "kitchen";
    private static final Log LOG = LogFactory.getLog(KitchenUIView.class);

    private Table ordersTable;
    private Table dishTable;
    private Button cookedBtn;
    private String selectedId;
    private Order selectedOrder;

    @Autowired
    private OrderContainer orderContainer;
    @Autowired
    private DishContainer dishesContainer;

    @Autowired
    private OrderRepository service;

    @Autowired
    private EventSystem eventSystem;

    @PostConstruct
    void init() {
        registerEvents();
        initData();
        initLayout();
    }

    @SuppressWarnings("serial")
    private void initLayout() {
        setMargin(true);
        setSpacing(true);
        // orders table
        ordersTable = new Table("Orders");
        ordersTable.setContainerDataSource(orderContainer);
        ordersTable.setVisibleColumns(OrderContainer.PROPERTIES);
        ordersTable.setColumnHeaders(OrderContainer.HEADERS);
        ordersTable.setSelectable(true);
        ordersTable.setWidth("100%");
        ordersTable.setHeight("100%");

        // table select listener
        ordersTable.addItemClickListener((ItemClickEvent.ItemClickListener) event -> {
            selectedId = (String) event.getItemId();
            selectedOrder = orderContainer.getItem(selectedId).getBean();
            initDishData(selectedOrder);
            LOG.info("Selected item id {" + selectedId + "}");
        });

        // Dishes table

        dishTable = new Table("Dishes", dishesContainer);
        dishTable.setVisibleColumns(DishContainer.PROPERTIES);
        dishTable.setColumnHeaders(DishContainer.HEADERS);
        // button bar
//        final AbstractLayout buttonBar = initButtonBar();
//        buttonBar.setWidth("100%");
        cookedBtn = new Button("Cooked", clickEvent -> setCooked());
        addComponent(ordersTable);
        addComponent(dishTable);
        addComponent(cookedBtn);
//        addComponent(buttonBar);
//        addComponent(editForm);
    }

    private void initDishData(Order selectedOrder) {
        dishesContainer.removeAllItems();

        dishesContainer.addAll(selectedOrder.getDishes());
    }

    private void registerEvents() {
        eventSystem.addListener(this);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {

    }

    private void initData() {
        // load data
        List<Order> all = service.findByStatus(Order.Status.TO_COOK);
        LOG.info(all);
        // clear table
        orderContainer.removeAllItems();
        // set table data
        orderContainer.addAll(all);
        dishesContainer.removeAllItems();
    }

    private void setCooked() {
        selectedOrder.setStatus(Order.Status.TO_DELIVERY);
        service.save(selectedOrder);
        eventSystem.fire(new ReloadEntriesEvent());
    }

    @Override
    public void reloadEntries(ReloadEntriesEvent event) {
        initData();
    }
}
