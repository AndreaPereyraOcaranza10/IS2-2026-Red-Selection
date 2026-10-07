package redselection.ejercicio_c.config;

import org.hibernate.envers.RevisionListener;
import redselection.ejercicio_c.entities.audit.Revision;

public class CustomRevisionListener implements RevisionListener {

    public void newRevision(Object revisionEntity) {
        final Revision revision = (Revision) revisionEntity;
    }

}