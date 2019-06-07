/**
 * Copyright (C) 2019 GZoltar contributors.
 * 
 * This file is part of GZoltar.
 * 
 * GZoltar is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * GZoltar is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with GZoltar. If
 * not, see <https://www.gnu.org/licenses/>.
 */
package com.gzoltar.internal.core.messaging;

import com.gzoltar.internal.core.events.IEventListener;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends ThreadedServer {

  private Service.ServiceFactory serviceFactory;

  public Server(final ServerSocket serverSocket, final Service.ServiceFactory serviceFactory) {
    super(serverSocket);
    this.serviceFactory = serviceFactory;
  }

  @Override
  protected final Runnable handle(final Socket s) {
    return new ServerDispatcher(s);
  }

  private class ServerDispatcher implements Runnable {

    private Socket socket;

    private Service service;

    public ServerDispatcher(final Socket s) {
      this.socket = s;
    }

    @Override
    public void run() {
      try {
        this.service = handshake();

        while (true) {
          Object o = new ObjectInputStream(this.socket.getInputStream()).readObject();

          if (dispatch(o)) {
            this.service.terminated();
            break;
          }
        }
      } catch (Throwable e) {
        if (this.service != null) {
          this.service.interrupted();
        }

        e.printStackTrace();
      }

      try {
        this.socket.close();
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }

    public Service handshake() throws Exception {
      Object o = new ObjectInputStream(this.socket.getInputStream()).readObject();

      if (!(o instanceof Message.HandshakeMessage)) {
        throw new Exception("First message should be a HandshakeMessage. Received instead: " + o);
      }

      String id = ((Message.HandshakeMessage) o).id;
      return serviceFactory.create(id);
    }

    private boolean dispatch(final Object o) {
      IEventListener eventListener = service.getEventListener();

      if (o instanceof Message.ByeMessage) {
        eventListener.endSession();
        return true;
      } else if (o instanceof Message.EndTransactionMessage) {
        Message.EndTransactionMessage etm = (Message.EndTransactionMessage) o;
        eventListener.endTransaction(etm.transaction);
      } else if (o instanceof Message.AddProbeGroupMessage) {
        Message.AddProbeGroupMessage anm = (Message.AddProbeGroupMessage) o;
        eventListener.regiterProbeGroup(anm.probeGroup);
      }

      return false;
    }
  }
}