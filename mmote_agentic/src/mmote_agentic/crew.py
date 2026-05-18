from crewai import Agent, Crew, Process, Task, LLM
from crewai.project import CrewBase, agent, crew, task
from crewai.agents.agent_builder.base_agent import BaseAgent
from mmote_agentic.tools.storage import save_notes_to_sqlite


@CrewBase
class MmoteAgentic():
    """MmoteAgentic crew"""
    # Instead of letting CrewAI use default values, you explicitly instantiate the Gemini model via code
    """
    def __init__(self):
        # Configure model parameters directly here
        self.llm = ChatGoogleGenerativeAI(
            model="gemini-2.5-flash",  # Or latest 2026 stable Flash variant
            temperature=0.2,          # Keep creativity low for technical data
            max_output_tokens=1000,   # Caps the generation length per single call
        )

    # Then assign this LLM configuration to your agents inside your agent initialization methods
    """

    agents: list[BaseAgent]
    tasks: list[Task]

    # def __init__(self) -> None:
    #     self.llm = LLM(
    #         model="google/gemini-2.0-flash-lite-001",
    #         temperature=0.15
    #     )

    @agent
    def note_researcher(self) -> Agent:
        return Agent(config=self.agents_config['note_researcher'], verbose=True)

    @agent
    def note_parser(self) -> Agent:
        return Agent(config=self.agents_config['note_parser'],  verbose=True)

    @agent
    def note_creator(self) -> Agent:
        return Agent(config=self.agents_config['note_creator'],  verbose=True)

    @task
    def research_task(self) -> Task:
        return Task(config=self.tasks_config['research_task'])

    @task
    def parsing_task(self) -> Task:
        return Task(config=self.tasks_config['parsing_task'])

    @task
    def creation_task(self) -> Task:
        return Task(
            config=self.tasks_config['creation_task'],
            # This triggers automatically the second this specific task successfully finishes
            callback=lambda task_output: save_notes_to_sqlite(task_output.raw)
        )

    @crew
    def crew(self) -> Crew:
        return Crew(
            agents=self.agents,
            tasks=self.tasks,
            process=Process.sequential,
            verbose=True
        )
