package com.intel.bigdata.agent;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.agent.Agent;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.common.collect.ImmutableMap;
import com.intel.bigdata.agent.AppExecutors.ExecutorFactory;
import com.intel.bigdata.common.protocol.*;
import com.intel.bigdata.common.util.SpringExtension;
import com.intel.bigdata.common.util.platform.LocalFS;
import com.intel.bigdata.common.util.platform.PackageManager;
import com.intel.bigdata.common.util.platform.ProcessManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: holm
 * Date: 10/17/13
 * Time: 1:23 PM
 * To change this template use File | Settings | File Templates.
 */
@Component("RequestExecutor")
@Scope("prototype")
public class RequestExecutor extends UntypedActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    private ProcessManager processManager;

    private PackageManager packageManager;

    private LocalFS localFS;

    private Agent<AgentConfig> agentConfig;

    private final ActorRef sequAppExecutor = getContext().actorOf(
            SpringExtension.SpringExtProvider.get(getContext().system()).props("SequAppExecutor"), "sequAppExecutor");

    @Inject
    @Required
    public void setAgentConfig(@Named("agentConfig") Agent<AgentConfig> agentConfig) {
        this.agentConfig = agentConfig;
    }

    @Autowired
    @Required
    void setProcessManager(ProcessManager processManager) {
        this.processManager = processManager;
    }

    @Autowired
    @Required
    void setPackageManager(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Autowired
    @Required
    public void setLocalFS(LocalFS localFS) {
        this.localFS = localFS;
    }

    @Override
    public void onReceive(Object msg) throws Exception {
        if (msg instanceof AgentRequest) {
            final AgentRequest agentRequest = (AgentRequest) msg;
            Object payload = agentRequest.getPayload();
            log.info("Agent request: {}", agentRequest);
            if (payload instanceof CommandRequest) {
                getSender().tell(
                        executeCommand(
                                agentRequest.getSessionId(),
                                agentConfig.get().getHostIdentifier(),
                                (CommandRequest) payload),
                        getSelf()
                    );

            } else if (payload instanceof ConfigureRequest) {
                getSender().tell(
                        new AgentResponse(agentRequest.getSessionId(), agentConfig.get().getHostIdentifier(),
                                configure((ConfigureRequest) payload)), getSelf());
            } else if (payload instanceof AppAction) {
                AppAction appPayload = (AppAction) payload;
                ExecutorFactory ef = new ExecutorFactory(appPayload);
                String type = ef.getType();
                if (type.equals(ExecType.Sequence.toString())) {
                    sequAppExecutor.tell(agentRequest, getSender());
                } else if (type.equals(ExecType.Concurrent.toString())) {
                    String actorName = ef.getActorName();
                    ActorSelection targetActor = context().actorSelection(getContext().system().child("agentDispatcher") + "/" + actorName);
                    targetActor.tell(agentRequest, getSender());
                }

            } else {
                unhandled(msg);
            }
        } else {
            unhandled(msg);
        }
    }

    private AgentResponse executeCommand(String commandId, String nodeId, CommandRequest req) {
        ImmutableMap<String, State> processStatusChanges = null;
        Result status = Result.SUCCESS;
        String details = String.format("Command request [%s, %s] completed successfully",
                req.getTarget(), req.getAction());

        try {
            switch (req.getAction()) {
                case START:
                    processManager.start(req.getTarget());
                    // Add process status changes
                    processStatusChanges = ImmutableMap.of(req.getTarget(), State.STARTED());
                    break;
                case STOP:
                    processManager.stop(req.getTarget());
                    // Add process status changes
                    processStatusChanges = ImmutableMap.of(req.getTarget(), State.STOPPED());
                    break;
                case INSTALL:
                    packageManager.install(req.getTarget());
                    break;
                case UNINSTALL:
                    packageManager.uninstall(req.getTarget());
                    break;
                case STATUS:
                    details = processManager.status(req.getTarget());
                    break;
                default:
                    this.unhandled(req);
            }
        } catch (Exception e) {
            // TODO: throw exception
            status = Result.FAILED;
            details = e.toString();
        }

        log.info("Command result {}, {}", status, details);
        return new AgentResponse(commandId, nodeId, new CommandResult(status, details), processStatusChanges);
    }

    public CommandResult configure(ConfigureRequest request) {
        boolean res = localFS.makeFile(request.getTarget(), request.getContent());
        return new CommandResult(res ? Result.SUCCESS : Result.FAILED, "");
    }
}
