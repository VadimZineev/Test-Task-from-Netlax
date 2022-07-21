package com.example.TestTaskNatlex.dao;

import com.example.TestTaskNatlex.enums.ExecutionStatus;
import com.example.TestTaskNatlex.models.persistence.Attachment;
import com.example.TestTaskNatlex.models.persistence.GeoClass;
import com.example.TestTaskNatlex.models.persistence.Job;
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
    public int saveFile(String content, String fileName, ExecutionStatus status, String uuid) {
        Session session = sessionFactory.getCurrentSession();
        Attachment attachment = new Attachment();
        attachment.setContext(content);
        attachment.setName(fileName);
        attachment.setStatus(status);
        attachment.setGuid(uuid);
        return (Integer) session.save(attachment);
    }

    @Transactional
    public Attachment getAttachmentById(int id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(Attachment.class, id);
    }

    @Transactional
    public List<Job> findAttachmentWithStatusNotStarted() {
        Session session = sessionFactory.getCurrentSession();
        List<Job> jobs = session.createQuery("from Job where statusExport = 6 and statusImport = 3").list();
        jobs.forEach(job -> {
            job.setStatusExport(ExecutionStatus.IN_PROGRESS);
        });
        return jobs;
    }

    @Transactional
    public void updateStatusJob(String uuid, ExecutionStatus status, boolean export) {
        Session session = sessionFactory.getCurrentSession();
        Optional<Job> jobInOptional = session.createQuery("from Job where guid = :guid")
                .setParameter("guid", uuid)
                .stream()
                .findFirst();
        var job = jobInOptional.get();
        if (export) {
            job.setStatusExport(status);
        } else job.setStatusImport(status);
        session.save(job);
    }

    @Transactional
    public ExecutionStatus getAttachmentStatus(Integer id) {
        Session session = sessionFactory.getCurrentSession();
        Attachment attachment = session.get(Attachment.class, id);
        try {
            return attachment.getStatus();
        } catch (NullPointerException e) {
            log.error(e.getMessage(), e);
            return ExecutionStatus.SECTION_NOT_FOUND;
        }
    }

    @Transactional
    public Integer getIdJob(String uuid, String name) {
        Session session = sessionFactory.getCurrentSession();
        Job job = new Job();
        job.setName(name);
        job.setGuid(uuid);
        job.setStatusImport(ExecutionStatus.IN_PROGRESS);
        job.setStatusExport(ExecutionStatus.NOT_STARTED);
        return (Integer) session.save(job);
    }

    @Transactional
    public List<Attachment> findJobWithStatusInProgress() {
        Session session = sessionFactory.getCurrentSession();
        List<Attachment> attachmentList = new ArrayList<>();
        List<String> jobGuidList = session.createQuery("SELECT guid FROM Job where statusExport = 4 and statusImport = 3").list();
        jobGuidList.forEach(guid -> {
            Optional<Attachment> attachmentOptional = session.createQuery("from Attachment where status = 3 and guid = :guid").setParameter("guid", guid).stream().findFirst();
            attachmentList.add(attachmentOptional.get());
        });
        return attachmentList;
    }

    @Transactional
    public Job getJob(Integer id) {
        Session session = sessionFactory.getCurrentSession();
        return session.get(Job.class, id);
    }
}