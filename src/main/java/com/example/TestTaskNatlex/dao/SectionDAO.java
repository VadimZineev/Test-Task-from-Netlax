package com.example.TestTaskNatlex.dao;

import com.example.TestTaskNatlex.models.persistence.GeoClass;
import com.example.TestTaskNatlex.models.persistence.Section;
import com.example.TestTaskNatlex.models.pojo.SectionPOJO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;

@Component
public class SectionDAO {

    private final SessionFactory sessionFactory;

    @Autowired
    public SectionDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public int save(SectionPOJO sectionPOJO) {
        Session session = sessionFactory.getCurrentSession();
        Section section = new Section();
        section.setName(sectionPOJO.getName());
        int sectionId = (Integer) session.save(section);

        var geoClassList = sectionPOJO.getGeoClassPOJOList();
        geoClassList.forEach((geoClassPOJO -> {
            GeoClass geoClass = new GeoClass();
            geoClass.setSectionId(sectionId);
            geoClass.setName(geoClassPOJO.getName());
            geoClass.setCode(geoClassPOJO.getCode());
            session.save(geoClass);
        }));
        return sectionId;
    }

//    @Transactional
//    public List<SectionPOJO> showAll() {
//        Session session = sessionFactory.getCurrentSession();
//        return session.createQuery("SELECT s.name, g.name, g.code FROM geoclass g " +
//                + " JOIN section s on g.section_id = s.id", SectionPOJO.class).getResultList();
//    }


//    @Transactional(readOnly = true)
//    public List<Section> getListSectionsByCode(String code) {
//        Session session = sessionFactory.getCurrentSession();
//        return session.createQuery("", S);
//    }
}
