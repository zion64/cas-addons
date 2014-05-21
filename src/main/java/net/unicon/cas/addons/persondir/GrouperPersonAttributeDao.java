package net.unicon.cas.addons.persondir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.services.persondir.IPersonAttributes;
import org.jasig.services.persondir.support.AttributeNamedPersonImpl;
import org.jasig.services.persondir.support.BasePersonAttributeDao;

import edu.internet2.middleware.grouperClient.api.GcGetGroups;
import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClient.ws.beans.WsGroup;

/**
 * Class implementing a minimal <code>IPersonAttributeDao</code> API only used by CAS which simply reads all the groups from Grouper repository
 * for a given principal and adopts them to <code>IPersonAttributes</code> instance. All other unimplemented methods throw <code>UnsupportedOperationException</code>
 * <p/>
 * This implementation uses Grouper's <i>grouperClient</i> library to query Grouper's back-end repository.
 * <p/>
 * Note: This class extends the adapter implementing deprecated methods of <code>IPersonAttributeDao</code> which is scheduled to be removed
 * in person-directory 1.6
 *
 * Note: All the Grouper server connection configuration for grouperClient is defined in <i>grouper.client.properties</i> file and must be available
 * in client application's (CAS web application) classpath.
 *
 * @author Dmitriy Kopylenko
 * @author Unicon, inc.
 * @since 0.9
 */
public class GrouperPersonAttributeDao extends BasePersonAttributeDao {

	public static final String DEFAULT_GROUPER_ATTRIBUTES_KEY = "grouperGroups";

	private String grouperAttributesKeyName = DEFAULT_GROUPER_ATTRIBUTES_KEY;

	public void setGrouperAttributesKeyName(String grouperAttributesKeyName) {
		this.grouperAttributesKeyName = grouperAttributesKeyName;
	}

	@Override
	public IPersonAttributes getPerson(String subjectId) {
		final GcGetGroups groupsClient = new GcGetGroups().addSubjectId(subjectId);
		final Map<String, List<Object>> grouperGroupsAsAttributesMap = new HashMap<String, List<Object>>(1);
		final List<Object> groupsList = new ArrayList<Object>();
		grouperGroupsAsAttributesMap.put("grouperGroups", groupsList);
		final IPersonAttributes personAttributes = new AttributeNamedPersonImpl(grouperGroupsAsAttributesMap);

		//Now retrieve and populate the attributes (groups from Grouper)
		for (WsGetGroupsResult groupsResult : groupsClient.execute().getResults()) {
			for (WsGroup group : groupsResult.getWsGroups()) {
				groupsList.add(group.getName());
			}
		}
		return personAttributes;
	}


	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getPossibleUserAttributeNames() {
		return Collections.EMPTY_SET;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<String> getAvailableQueryAttributes() {
		return Collections.EMPTY_SET;
	}

	@Override
	public Set<IPersonAttributes> getPeople(Map<String, Object> stringObjectMap) {
		throw new UnsupportedOperationException("This method is not implemented.");
	}

	@Override
	public Set<IPersonAttributes> getPeopleWithMultivaluedAttributes(Map<String, List<Object>> stringListMap) {
		throw new UnsupportedOperationException("This method is not implemented.");
	}
}
