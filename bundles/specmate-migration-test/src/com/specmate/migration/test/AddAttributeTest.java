package com.specmate.migration.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;

import com.specmate.common.SpecmateException;
import com.specmate.migration.api.IMigratorService;
import com.specmate.migration.test.attributeadded.testmodel.artefact.ArtefactFactory;
import com.specmate.migration.test.attributeadded.testmodel.artefact.Diagram;
import com.specmate.migration.test.attributeadded.testmodel.base.Folder;
import com.specmate.migration.test.baseline.testmodel.base.BaseFactory;
import com.specmate.migration.test.support.AttributeAddedModelProviderImpl;
import com.specmate.migration.test.support.BaselineModelProviderImpl;
import com.specmate.migration.test.support.ServiceController;
import com.specmate.model.support.util.SpecmateEcoreUtil;
import com.specmate.persistency.IPackageProvider;
import com.specmate.persistency.IPersistencyService;
import com.specmate.persistency.ITransaction;
import com.specmate.persistency.cdo.internal.CDOPersistencyService;
import com.specmate.persistency.cdo.internal.config.CDOPersistenceConfig;
import com.specmate.urihandler.IURIFactory;

public class AddAttributeTest {
	private static BundleContext context;
	private static ServiceController<CDOPersistencyService> persistencyServiceController;
	private static ServiceController<BaselineModelProviderImpl> baselineModelController;
	private static ServiceController<AttributeAddedModelProviderImpl> attributeAddedModelController;
	private static IPersistencyService persistencyService;
	private static IMigratorService migratorService;
	
	@BeforeClass
	public static void init() throws Exception {
		context = FrameworkUtil.getBundle(AddAttributeTest.class).getBundleContext();
		
		
		/*Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(CDOPersistenceConfig.KEY_JDBC_CONNECTION, "jdbc:h2:mem:testdb");
		properties.put(CDOPersistenceConfig.KEY_JDBC_CONNECTION, "jdbc:h2:./database/specmate");
		properties.put(CDOPersistenceConfig.KEY_REPOSITORY_NAME, "testrepo");
		properties.put(CDOPersistenceConfig.KEY_RESOURCE_NAME, "r1");
		properties.put(CDOPersistenceConfig.KEY_USER_RESOURCE_NAME, "r2");*/
	
		
		baselineModelController = new ServiceController<>(context);
		baselineModelController.register(IPackageProvider.class, BaselineModelProviderImpl.class, null);
		attributeAddedModelController = new ServiceController<>(context);
		attributeAddedModelController.register(IPackageProvider.class, AttributeAddedModelProviderImpl.class, null);
		persistencyServiceController = new ServiceController<>(context); 
		migratorService = getMigratorService();
		
		activatePersistency(baselineModelController.getService());
		addBaselinedata();
		deactivatePersistency();
	}
	
	@AfterClass
	public static void shutdown() {
		baselineModelController.unregister();
		attributeAddedModelController.unregister();
	}
	
	private static void activatePersistency(IPackageProvider p) throws Exception {
		persistencyServiceController.register(IPersistencyService.class, CDOPersistencyService.class, null);
		persistencyService = persistencyServiceController.getService();
		CDOPersistencyService cdop = (CDOPersistencyService) persistencyService;
		startSupportServices(cdop, p);
		cdop.activate();
	}
	
	private static void deactivatePersistency() {
		CDOPersistencyService cdop = (CDOPersistencyService) persistencyService;
		cdop.deactivate();
		persistencyServiceController.unregister();
	}
	
	@Test 
	public void testNeedsMigration() throws Exception {
		activatePersistency(baselineModelController.getService());
		assertFalse(migratorService.needsMigration());
		migratorService.setModelProviderService(attributeAddedModelController.getService());
		assertTrue(migratorService.needsMigration());
		deactivatePersistency();
	}
	
	@Test
	public void doMigration() throws Exception {
		activatePersistency(baselineModelController.getService());
		ITransaction transaction = persistencyService.openTransaction();
		Resource resource = transaction.getResource();
		EObject root = SpecmateEcoreUtil.getEObjectWithName("root", resource.getContents());
		assertNotNull(root);
		EObject diagram = SpecmateEcoreUtil.getEObjectWithName("d0", root.eContents());
		assertNotNull(diagram);
		assertNull(SpecmateEcoreUtil.getAttributeValue(root, "id", String.class));
		assertNull(SpecmateEcoreUtil.getAttributeValue(diagram, "id", String.class));
		transaction.close();
		deactivatePersistency();
		
		migratorService.setModelProviderService(attributeAddedModelController.getService());
		migratorService.doMigration();
		
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("service.ranking", 10);
		attributeAddedModelController.modify(properties);
		
		activatePersistency(attributeAddedModelController.getService());
		
		transaction = persistencyService.openTransaction();
		resource = transaction.getResource();
		root = SpecmateEcoreUtil.getEObjectWithName("root", resource.getContents());
		assertNotNull(root);
		
		assertTrue(root instanceof Folder);
		Folder rootFolder = (Folder) root;
		rootFolder.setId("f0");
		
		diagram = SpecmateEcoreUtil.getEObjectWithName("d0", rootFolder.eContents());
		assertNotNull(diagram);
		assertTrue(diagram instanceof Diagram);
		Diagram d0 = (Diagram) diagram;
		d0.setId("d0");
		
		Diagram d1 = ArtefactFactory.eINSTANCE.createDiagram();
		d1.setName("d1");
		d1.setId("d1");
		
		rootFolder.getContents().add(d1);
		transaction.commit();
		deactivatePersistency();
	}
	
	private static IMigratorService getMigratorService() throws InterruptedException {
		ServiceTracker<IMigratorService, IMigratorService> migratorServiceTracker =
				new ServiceTracker<>(context, IMigratorService.class.getName(), null);
		
		migratorServiceTracker.open();
		IMigratorService migratorService = migratorServiceTracker.waitForService(10000);
		Assert.assertNotNull(migratorService);
		return migratorService;
	}
	
	private static void startSupportServices(CDOPersistencyService ps, IPackageProvider ip) throws InterruptedException {
		ServiceTracker<LogService, LogService> logServiceTracker =
				new ServiceTracker<>(context, LogService.class.getName(), null);
		
		logServiceTracker.open();
		LogService logService = logServiceTracker.waitForService(10000);
		Assert.assertNotNull(logService);
		
		ServiceTracker<IURIFactory, IURIFactory> iuriServiceTracker =
				new ServiceTracker<>(context, IURIFactory.class.getName(), null);
		
		iuriServiceTracker.open();
		IURIFactory iuriService = iuriServiceTracker.waitForService(10000);
		Assert.assertNotNull(iuriService);
		
		ServiceTracker<EventAdmin, EventAdmin> eventAdminTracker =
				new ServiceTracker<>(context, EventAdmin.class.getName(), null);
		
		eventAdminTracker.open();
		EventAdmin eventAdminService = eventAdminTracker.waitForService(10000);
		Assert.assertNotNull(eventAdminService);
		
		ps.setEventAdmin(eventAdminService);
		ps.setLogService(logService);
		ps.setUriFactory(iuriService);
		ps.addModelProvider(ip);
	}
	
	private static void addBaselinedata() throws SpecmateException, InterruptedException {
		ITransaction transaction = persistencyService.openTransaction();
		Resource resource = transaction.getResource();
		EObject root = SpecmateEcoreUtil.getEObjectWithName("root", resource.getContents());
		
		if(root == null) {
			com.specmate.migration.test.baseline.testmodel.base.Folder f = BaseFactory.eINSTANCE.createFolder();
			f.setName("root");
			loadBaselineTestdata(f);
			
			transaction.getResource().getContents().add(f);
			
			try {
				transaction.commit();
			}
			catch(SpecmateException e) {
				System.out.println(e.getMessage());
			}
			
		}
	}
	
	private static void loadBaselineTestdata(com.specmate.migration.test.baseline.testmodel.base.Folder root) {
		com.specmate.migration.test.baseline.testmodel.artefact.Diagram d1 = com.specmate.migration.test.baseline.testmodel.artefact.ArtefactFactory.eINSTANCE.createDiagram();
		d1.setName("d0");
		d1.setTested(true);
		
		root.getContents().add(d1);
	}
	
}
