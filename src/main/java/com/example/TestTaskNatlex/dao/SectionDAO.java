package com.example.TestTaskNatlex.dao;

import com.example.TestTaskNatlex.models.enums.ExecutionStatus;
import com.example.TestTaskNatlex.models.persistence.Attachment;
import com.example.TestTaskNatlex.models.persistence.GeoClass;
import com.example.TestTaskNatlex.models.persistence.Section;
import com.example.TestTaskNatlex.models.response.GeoClassResponse;
import com.example.TestTaskNatlex.models.response.SectionResponse;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Slf4j
@Component
public class SectionDAO {

    private final SessionFactory sessionFactory;


    @Autowired
    public SectionDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Transactional
    public int save(Section section) {
        Session session = sessionFactory.getCurrentSession();
        int sectionId = (Integer) session.save(section);
        var geoClassList = section.getGeoClassList();
        try {
            geoClassList.forEach((geoClass) -> {
                geoClass.setSectionId(sectionId);
                session.save(geoClass);
            });
        } catch (NullPointerException e) {
            log.info("GeoClass list is empty!");
        }
        return sectionId;
    }

    @Transactional(readOnly = true)
    public List<SectionResponse> showAll() {
        Session session = sessionFactory.getCurrentSession();
        List<SectionResponse> sectionResponseList = new ArrayList<>();

        List<Section> sectionList = session.createQuery("FROM Section ").list();
        sectionList.forEach(section -> {
            SectionResponse sectionResponse = new SectionResponse();
            sectionResponse.setName(section.getName());
            List<GeoClass> geoClassList = session.createQuery("from GeoClass where sectionId = :id")
                    .setParameter("id", section.getId())
                    .list();
            List<GeoClassResponse> geoClassResponseList = new ArrayList<>();
            geoClassList.forEach(gc -> {
                GeoClassResponse geoClassResponse = new GeoClassResponse();
                geoClassResponse.setName(gc.getName());
                geoClassResponse.setCode(gc.getCode());
                geoClassResponseList.add(geoClassResponse);
            });
            sectionResponse.setGeoClassResponseList(geoClassResponseList);
            sectionResponseList.add(sectionResponse);
        });
        return sectionResponseList;
    }

    @Transactional
    public ExecutionStatus edit(Section section) {
        Session session = sessionFactory.getCurrentSession();
        try {
            List<GeoClass> geoClassList = section.getGeoClassList();
            geoClassList.forEach(geoClass -> {
                Query update = session.createQuery("update GeoClass set code = :code where name = :name");
                update.setParameter("code", geoClass.getCode());
                update.setParameter("name", geoClass.getName());
                update.executeUpdate();
            });
            return ExecutionStatus.SUCCESS;
        } catch (NoSuchElementException e) {
            log.info("Element is not found!");
            return ExecutionStatus.SECTION_NOT_FOUND;
        }
    }

    @Transactional
    public ExecutionStatus delete(Section section) {
        Session session = sessionFactory.getCurrentSession();
        try {
            Optional<Section> sectionOptional = session.createQuery("from Section where name = :name")
                    .setParameter("name", section.getName())
                    .list()
                    .stream()
                    .findFirst();
            Query deleteGeoClass = session.createQuery("delete GeoClass where sectionId = :id")
                    .setParameter("id", sectionOptional.get().getId());
            deleteGeoClass.executeUpdate();

            Query deleteSection = session.createQuery("delete Section where id = :id")
                    .setParameter("id", sectionOptional.get().getId());
            deleteSection.executeUpdate();

            return ExecutionStatus.SUCCESS;
        } catch (NoSuchElementException e) {
            log.info("Element is not found!");
            return ExecutionStatus.SECTION_NOT_FOUND;
        }
    }

    @Transactional
    public List<SectionResponse> showSectionByCode(String code) {
        Session session = sessionFactory.getCurrentSession();
        List<SectionResponse> sectionResponseList = new ArrayList<>();
        Query selectSectionId = session.createQuery("from GeoClass where code = :code");
        selectSectionId.setParameter("code", code);
        List<GeoClass> geoClassList = selectSectionId.list();
        geoClassList.forEach(geoClass -> {
            Query selectSectionById = session.createQuery("from Section where id = :id");
            selectSectionById.setParameter("id", geoClass.getSectionId());
            List<Section> sectionList = selectSectionById.list();
            sectionList.forEach(section -> {
                SectionResponse sectionResponse = new SectionResponse();
                sectionResponse.setName(section.getName());
                List<GeoClass> geoClasses = session.createQuery("from GeoClass where sectionId = :id")
                        .setParameter("id", section.getId())
                        .list();
                List<GeoClassResponse> geoClassResponseList = new ArrayList<>();
                geoClasses.forEach(gc -> {
                    GeoClassResponse geoClassResponse = new GeoClassResponse();
                    geoClassResponse.setName(gc.getName());
                    geoClassResponse.setCode(gc.getCode());
                    geoClassResponseList.add(geoClassResponse);
                });
                sectionResponse.setGeoClassResponseList(geoClassResponseList);
                sectionResponseList.add(sectionResponse);
            });
        });
        return sectionResponseList;
    }

    @Transactional
    public void saveFromFile(HashMap<Integer, List<String>> map, Integer attachmentId) {
        Session session = sessionFactory.getCurrentSession();
        map.values().forEach(v -> {
            if (!v.isEmpty()) {
                Section section = new Section();
                List<GeoClass> geoClassList = new ArrayList<>();
                section.setName(v.get(0));
                section.setAttachmentId(attachmentId);
                v.remove(0);
                for (int i = 0; i < v.size(); i = i + 2) {
                    GeoClass geoClass = new GeoClass();
                    geoClass.setName(v.get(i));
                    geoClass.setCode(v.get(i + 1));
                    geoClassList.add(geoClass);
                }
                section.setGeoClassList(geoClassList);
                this.save(section);
                Attachment attachment = session.get(Attachment.class, attachmentId);
                attachment.setStatus(ExecutionStatus.DONE);
                session.save(attachment);
            }
        });
    }

    @Transactional
    public int saveFile(String content, String fileName, ExecutionStatus status)  {
        Session session = sessionFactory.getCurrentSession();
        Attachment attachment = new Attachment();
        attachment.setContext(content);
        attachment.setName(fileName);
        attachment.setStatus(status);
        return (Integer) session.save(attachment);
    }

    @Transactional
    public Attachment getAttachmentById(int id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(Attachment.class, id);
    }

    @Transactional
    public Optional<Attachment> findAttachmentWithStatusInProgress() {
        Session session = sessionFactory.getCurrentSession();
        Query selectAttachmentByStatus = session.createQuery("from Attachment where status = :status");
        selectAttachmentByStatus.setParameter("status", ExecutionStatus.IN_PROGRESS);
        return selectAttachmentByStatus.list().stream().findFirst();
    }

    @Transactional
    public void updateStatus(Integer id, ExecutionStatus status) {
        Session session = sessionFactory.getCurrentSession();
        Attachment attachment = session.get(Attachment.class, id);
        attachment.setStatus(status);
        session.save(attachment);
    }

    @Transactional
    public ExecutionStatus getAttachmentStatus(Integer id) {
        Session session = sessionFactory.getCurrentSession();
        Attachment attachment = session.get(Attachment.class, id);
        return attachment.getStatus();
    }
}