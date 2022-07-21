package com.example.TestTaskNatlex.dao;

import com.example.TestTaskNatlex.enums.ExecutionStatus;
import com.example.TestTaskNatlex.models.persistence.Attachment;
import com.example.TestTaskNatlex.models.persistence.GeoClass;
import com.example.TestTaskNatlex.models.persistence.Job;
import com.example.TestTaskNatlex.models.persistence.Section;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Repository "Job" and "Attachment"
 */
@Slf4j
@Component
public class JobDAO {
    private final SessionFactory sessionFactory;
    private final SectionDAO sectionDAO;

    @Autowired
    public JobDAO(SessionFactory sessionFactory, SectionDAO sectionDAO) {
        this.sessionFactory = sessionFactory;
        this.sectionDAO = sectionDAO;
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
                sectionDAO.save(section);
                Attachment attachment = session.get(Attachment.class, attachmentId);
                attachment.setStatus(ExecutionStatus.DONE);
                session.save(attachment);
            }
        });
    }
}
