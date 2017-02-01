package br.com.battlebits.commons.core.server.loadbalancer;

import br.com.battlebits.commons.core.server.loadbalancer.element.LoadBalancerObject;

public interface LoadBalancer<T extends LoadBalancerObject> {

	public T next();

}
