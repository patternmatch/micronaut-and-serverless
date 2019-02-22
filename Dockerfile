FROM python:3.6

RUN apt-get update && \
    apt-get install -y && \
    rm -rf /var/lib/apt/lists/*
RUN pip install awscli
RUN curl -sL https://deb.nodesource.com/setup_8.x > node_install.sh
RUN chmod +x ./node_install.sh
RUN ./node_install.sh
RUN curl -sS http://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add -
RUN echo "deb http://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list
RUN apt-get update
RUN apt-get install -y apt-utils nodejs yarn

# Install serverless cli
RUN yarn global add serverless

RUN mkdir /workspace
RUN useradd -r -u 1000 appuser \
  && mkdir -p /home/appuser
RUN chown -R appuser:appuser /workspace /home/appuser
USER appuser
