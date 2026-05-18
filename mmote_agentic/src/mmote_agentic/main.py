#!/usr/bin/env python
import sys
import time
import warnings

from datetime import datetime

from mmote_agentic.crew import MmoteAgentic

warnings.filterwarnings("ignore", category=SyntaxWarning, module="pysbd")

# This main file is intended to be a way for you to run your
# crew locally, so refrain from adding unnecessary logic into this file.
# Replace with inputs you want to test with, it will automatically
# interpolate any tasks and agents information


def run():
    raw_input_string = """geopolitics, history, philosophy, astronomy, Indian indie songs, psychology, mentalists, economy, naval ravikant, elon musk, google deepmind projects, nvidia future concepts like omniverse etc & what impact it can have, robotics, artificial intelligence, effects of ai in economics, effects of ai in humanity as a whole, philosophical discussions about survival of humanity & the causes of nature with machines etc, sci-fi movies, math & physics, biology, science, p vs np philosophical discussions, famous podcasts & guests, nature, animals & birds, amazon rainforest, interconnectedness of the world & society, greed & desire, books, Yuval noah Harari, David Deutsch, journey & evolution of humans from monkeys the branching out to erectus, neanderthals & sapiens at last, the pure luck of dinosaurs going extinct after which its a lucky chance evolution gave mammals, how did even that fish came out of water, the current consciousness of humans, how did consciousness emerge in humans, stars in the universe, how the star dust are part of us like the element iron in our blood came from the stars, standard model of physics, quantum mechanics, entanglement in the universe & in the atoms, space & gravity, hindu mythology & vedas and its take on the universe & working of it, hindu understanding of time & space, hindu astrology, collective vs individual consciousness, the case against reality by Donald hoffman, evolution of human societies & politics & kingdoms, francis Fukuyama books on nature history of current state of politics, consumerism, marketing, nature of wars, the dark economic reason of wars, human peace, can a society i.e a group of people can ever be without conflict i.e at peace?, has evolution made us incompetent of peace at larger scale?, universe 25 experiment, jiddu krishnamurty philosophies on observer & nature of human conflict, philosophies of sadhguru i.e jaggi Vasudev, information & thoughts on the nature of life & death, microbiology, mix of modern science of death & hindu way of understanding it"""

    # Parse CSV clean of padding white spaces
    topics_list = [topic.strip()
                   for topic in raw_input_string.split(",") if topic.strip()]

    # Map each topic entry to the dictionary keyword structure matching the yaml context variable
    inputs_collection = [{"topic": topic} for topic in topics_list]

    print(
        f"Loaded {len(inputs_collection)} dynamic topic structures for processing.")

    # Instantiate the structural team once
    crew_engine = MmoteAgentic().crew()

    for idx, topic in enumerate(topics_list):
        print(
            f"[{idx + 1}/{len(topics_list)}] Initiating processing pipeline for: {topic}")

        try:
            # Run one individual topic through the full agent assembly line
            result = crew_engine.kickoff(inputs={"topic": topic})
            print(f"Successfully committed data for: {topic}")

            # CRITICAL: Sleep for 40 seconds between topics to allow your Free Tier
            # Requests-Per-Minute (RPM) counter to completely reset.
            print("Pacing execution. Cooling down API socket for 40 seconds...")
            time.sleep(40)

        except Exception as e:
            print(f"API Rate limit hit or failure on topic '{topic}': {e}")
            print(
                "Executing emergency cooling sleep for 60 seconds before auto-retry...")
            time.sleep(60)


if __name__ == "__main__":
    run()
