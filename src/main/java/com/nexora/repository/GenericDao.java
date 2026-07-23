package com.nexora.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

/**
 * DAO generique (pattern Repository) mutualisant le CRUD pour toutes les
 * entites. Chaque DAO concret herite de cette classe en precisant le type
 * d'entite, respectant le principe DRY et la responsabilite unique (SOLID).
 *
 * @param <T>  type de l'entite
 * @param <ID> type de la cle primaire
 */
public abstract class GenericDao<T, ID extends Serializable> {

    @PersistenceContext(unitName = "nexoraPU")
    protected EntityManager em;

    private final Class<T> entityClass;

    @SuppressWarnings("unchecked")
    protected GenericDao() {
        this.entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public T save(T entity) {
        em.persist(entity);
        return entity;
    }

    public T update(T entity) {
        return em.merge(entity);
    }

    public void delete(T entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    public Optional<T> findById(ID id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    public List<T> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);
        return em.createQuery(cq.select(root)).getResultList();
    }

    public long count() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        cq.select(cb.count(cq.from(entityClass)));
        return em.createQuery(cq).getSingleResult();
    }

    protected EntityManager em() {
        return em;
    }

    protected Class<T> entityClass() {
        return entityClass;
    }
}
