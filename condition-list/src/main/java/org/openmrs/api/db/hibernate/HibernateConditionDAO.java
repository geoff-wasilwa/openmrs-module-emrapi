/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Condition;
import org.openmrs.Patient;
import org.openmrs.api.db.ConditionDAO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class HibernateConditionDAO implements ConditionDAO {

    protected static final Log log = LogFactory.getLog(ConditionDAO.class);
    /**
     * Hibernate session factory
     */

    private SessionFactory sessionFactory;

    /**
     * Set session factory
     *
     * @param sessionFactory
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    @Transactional
    public Condition saveOrUpdate(Condition condition) {
        sessionFactory.getCurrentSession().saveOrUpdate(condition);
        return condition;
    }

    @Override
    @Transactional(readOnly = true)
    public Condition getConditionByUuid(String uuid) {
        return (Condition) sessionFactory.getCurrentSession().createQuery("from Condition c where c.uuid = :uuid")
                .setString("uuid", uuid).uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Condition> getConditionHistory(Patient patient) {
        Query query = sessionFactory.getCurrentSession().createQuery("select con from Condition as con " +
                " inner join fetch con.concept as c left join c.names as name with name.conceptNameType = 'FULLY_SPECIFIED' " +
                " where con.patient.patientId = :patientId and con.voided = false  group by c.conceptId, con.conditionId order by name.name asc , con.dateCreated desc");
        query.setInteger("patientId", patient.getId());
        return query.list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Condition> getActiveConditions(Patient patient) {
        Query query = sessionFactory.getCurrentSession().createQuery("from Condition c where c.patient.patientId = :patientId and c.voided = false and c.endDate is null");
        query.setInteger("patientId", patient.getId());
        return query.list();
    }
}
