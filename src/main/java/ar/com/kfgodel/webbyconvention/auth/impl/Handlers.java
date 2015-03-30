package ar.com.kfgodel.webbyconvention.auth.impl;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.HandlerList;

import java.util.List;

/**
 * Utility class for handler list
 * Created by kfgodel on 29/03/15.
 */
public class Handlers {

    public static HandlerList asList(List<Handler> requestHandlers) {
        return asList(requestHandlers.toArray(new Handler[requestHandlers.size()]));
    }

    public static HandlerList asList(Handler... handlers) {
        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(handlers);
        return handlerList;
    }
}
